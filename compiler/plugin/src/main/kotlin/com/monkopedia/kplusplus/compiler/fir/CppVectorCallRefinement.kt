package com.monkopedia.kplusplus.compiler.fir

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.extensions.FirExtensionApiInternals
import org.jetbrains.kotlin.fir.extensions.FirFunctionCallRefinementExtension
import org.jetbrains.kotlin.fir.resolve.calls.candidate.CallInfo
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.fir.types.constructType
import org.jetbrains.kotlin.name.Name

/**
 * Refines container-facade calls (`cppVector<T>()`, `cppMap<K,V>()`, …) so the
 * call site sees the concrete generated binding type (e.g. std.Vector__Int,
 * std.Map__Int__Int) instead of the declared generic. The return type points
 * at the existing generated class, not a synthetic one, so this extension owns
 * no symbols. The concrete class comes from krapper's emitted `binding-index.json`
 * ([CppVectorMapping.resolveBinding]), never from a name this plugin reconstructs (#206). When
 * the binding is absent — or no index can be read — the call is left unrefined and
 * [CppVectorCallChecker] reports it.
 */
@OptIn(FirExtensionApiInternals::class)
class CppVectorCallRefinement(session: FirSession) :
    FirFunctionCallRefinementExtension(session) {

    override fun intercept(callInfo: CallInfo, symbol: FirNamedFunctionSymbol): CallReturnType? {
        val container = CppVectorMapping.containerForCallable(symbol, session) ?: return null
        val elements = CppVectorMapping.elementsOf(callInfo, session) ?: return null
        if (elements.size != container.arity) return null
        val resolved = CppVectorMapping.resolveBinding(container, elements)
            as? BindingResolution.Resolved ?: return null
        val bindingSymbol = session.symbolProvider
            .getClassLikeSymbolByClassId(resolved.classId)
            as? FirRegularClassSymbol ?: return null
        val typeRef = buildResolvedTypeRef {
            coneType = bindingSymbol.constructType(emptyArray(), isMarkedNullable = false)
        }
        return CallReturnType(typeRef)
    }

    override fun transform(
        call: FirFunctionCall,
        symbol: FirNamedFunctionSymbol
    ): FirFunctionCall = call

    override fun ownsSymbol(symbol: FirRegularClassSymbol): Boolean = false

    override fun anchorElement(symbol: FirRegularClassSymbol): KtSourceElement =
        error("kplusplus refinement owns no symbols")

    override fun restoreSymbol(call: FirFunctionCall, name: Name): FirRegularClassSymbol? = null
}
