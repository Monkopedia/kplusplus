package com.monkopedia.kplusplus.compiler

import com.monkopedia.kplusplus.compiler.fir.CppVectorMapping
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.util.callableId
import org.jetbrains.kotlin.ir.util.companionObject
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

/**
 * Lowers container-facade calls (`cppVector<T>()`, `cppMap<K,V>()`, …) to a
 * call to the generated binding's companion default-constructor factory
 * (e.g. `MemScope.Vector__Int()` / `MemScope.Map__Int__Int()`), reusing the
 * original call's MemScope extension receiver. Pairs with
 * CppVectorCallRefinement — the target binding is read directly off the
 * (refined) call type rather than hardcoded.
 *
 * Unified IR `arguments` model: for the factory (a companion member-extension)
 * arguments[0] is the dispatch receiver (the companion), arguments[1] the
 * extension receiver (the MemScope); for the original top-level facade
 * extension call arguments[0] is its MemScope receiver.
 */
class CppVectorIrExtension : IrGenerationExtension {
    private val CPP_TEMPLATE_ANNOTATION =
        ClassId(FqName("krapper"), Name.identifier("CppTemplate"))

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.transformChildrenVoid(object : IrElementTransformerVoidWithContext() {
            override fun visitCall(expression: IrCall): IrExpression {
                val callee = expression.symbol.owner
                // `callableId` THROWS (not returns null) for a function with no stable
                // callable id — e.g. a member of an anonymous `object` expression, which
                // the generated bindings now produce (the synthesized `operator fun
                // iterator()` returns an anonymous `Iterator<T>`). Such calls are never a
                // container facade, so treat the throw as "not a facade" and pass through.
                val callable = runCatching { callee.callableId }.getOrNull()
                    ?: return super.visitCall(expression)
                // Lower either a hardcoded std container facade (by callableId) or a
                // generic user-template facade (carrying `@krapper.CppTemplate`). The
                // rest of the lowering reads the concrete binding off the already-
                // refined call type, so it's identical for both.
                if (!CppVectorMapping.isContainerFacade(callable) &&
                    !callee.hasAnnotation(CPP_TEMPLATE_ANNOTATION)
                ) {
                    return super.visitCall(expression)
                }
                val scope = expression.arguments.getOrNull(0)
                    ?: return super.visitCall(expression)
                val bindingClass = expression.type.classOrNull?.owner
                    ?: return super.visitCall(expression)
                val companion = bindingClass.companionObject()
                    ?: return super.visitCall(expression)
                // Prefer the no-arg default-constructor factory (e.g.
                // `MemScope.Vector__Int()`). Some value types don't get a default
                // ctor wrapped (e.g. std::pair's default ctor isn't generated) —
                // fall back to the `_Holder` factory, which allocs storage without
                // constructing. That's correct for the facade's "fresh instance"
                // intent on trivially-constructible types (pair<int,int>, plain
                // structs); a non-trivial type with no wrapped default ctor would
                // need real construction (not yet hit — flagged in the matrix).
                val factory = companion.functions.firstOrNull {
                    it.name == bindingClass.name && it.parameters.size == 2
                } ?: companion.functions.firstOrNull {
                    it.name.asString() == "${bindingClass.name.asString()}_Holder" &&
                        it.parameters.size == 2
                } ?: return super.visitCall(expression)

                val builder = DeclarationIrBuilder(
                    pluginContext,
                    currentScope!!.scope.scopeOwnerSymbol,
                    expression.startOffset,
                    expression.endOffset
                )
                return builder.irCall(factory.symbol).apply {
                    arguments[0] = builder.irGetObject(companion.symbol)
                    arguments[1] = scope
                }
            }
        })
    }
}
