/*
 * Copyright 2026 Jason Monk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import clang.CXXMethodDecl
import clang.CXXRecordDecl
import clang.Decl
import clang.FunctionDecl
import clang.ParmVarDecl
import clang.TranslationUnitDecl
import com.monkopedia.krapper.generator.model.WrappedArgument
import com.monkopedia.krapper.generator.model.WrappedBase
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.WrappedMethod
import com.monkopedia.krapper.generator.model.WrappedTU
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.resolvedmodel.MethodType

// The C++-AST front-end's model construction (#44 brick 2): walk a parsed Clang AST on the
// kplusplus-generated libclang-cpp bindings and build :krapper_model's parse-output model —
// the same WrappedTU shape the libclang-C front-end (ModelFactories.kt) produces.

// IDENTITY CONVENTION: the libclang front-end keys identity fields (WrappedArgument.usr and
// the reducer's lookup maps) on libclang's USR string. This front-end's equivalent is the
// canonical-Decl id proven on clangwalk/decl-identity-probe (b9e19dc): normalize any decl to
// its canonical clang::Decl* (redeclaration-collapse) and read Decl::getID(), spelled
// "cpp:<id>". The two front-ends therefore NEVER agree on the literal identity string —
// Phase C's tree-diff treats identity fields as maskable and compares structure only.
private fun Decl.canonical(): Decl = (getCanonicalDecl() as? Decl) ?: this

private fun Decl.cppUsr(): String = "cpp:${canonical().getID()}"

// Construct a WrappedTU from the parsed translation unit. Top-level shape matches the
// libclang front-end (ModelFactories.mapAll): a WrappedClass per record and a
// MethodType.STATIC WrappedMethod per free function, both direct children of the TU.
fun buildWrappedTU(tuDecl: TranslationUnitDecl): WrappedTU {
    val tu = WrappedTU()
    for (decl in tuDecl.asDeclContext().decls()) {
        if (decl == null) continue
        // Skip the implicit builtin TU members (__int128_t, __builtin_va_list, ...) —
        // they aren't part of the parsed fixture.
        if (decl.isImplicit()) continue
        val record = decl.asCXXRecordDecl()
        if (record != null) {
            tu.addChild(buildClass(record))
            continue
        }
        val function = decl.asFunctionDecl()
        if (function != null && decl.asCXXMethodDecl() == null) {
            // A free function is a STATIC WrappedMethod with no parent class — the model's
            // namespace-level-function convention (see freeFunctionQualifiedName).
            tu.addChild(buildFunction(function))
        }
        // brick-3+: namespaces, typedefs, enums, global fields, class templates.
    }
    return tu
}

private fun buildClass(record: CXXRecordDecl): WrappedClass {
    val name = record.asNamedDecl().getNameAsString() ?: error("record without a name")
    // brick-3+: isAbstract + ClassMetadata (hasConstructor/hasCopyConstructor/...) — left at
    // the model's defaults until the ctor/dtor walk lands.
    val cls = WrappedClass(name)
    for (base in record.bases()) {
        if (base == null) continue
        val spelling = base.getType().getAsString() ?: continue
        // brick-3+: isPublic/isVirtualBase need the access-specifier accessors bound.
        cls.addChild(WrappedBase(WrappedType(spelling)))
    }
    for (method in record.methods()) {
        if (method == null) continue
        // Implicit members (Sema-injected copy ctor/assignment) aren't fixture surface.
        if (method.isImplicit()) continue
        // brick-3+: CXXConstructor/CXXDestructor/CXXConversion kinds map to
        // WrappedConstructor/WrappedDestructor/conversion methods; only plain methods now.
        if (method.getDeclKindName() != "CXXMethod") continue
        cls.addChild(buildMethod(method))
    }
    // brick-3+: FieldDecl children -> WrappedField.
    return cls
}

private fun buildMethod(method: CXXMethodDecl): WrappedMethod {
    val name = method.asNamedDecl().getNameAsString() ?: error("method without a name")
    val isConst = method.isConst()
    val spelling = method.getReturnType().getAsString() ?: error("method without a return")
    // Mirror the libclang front-end's return-const rule (ModelFactories.WrappedMethod):
    // a `const` method's constness only carries to a pointer/reference return; const on a
    // by-value return is meaningless for the temporary and breaks the Holder path (G8).
    val returnType = WrappedType(spelling).let {
        if (isConst && (it.isPointer || it.isReference)) WrappedType.const(it) else it
    }
    val methodType = if (method.isStatic()) MethodType.STATIC else MethodType.METHOD
    return WrappedMethod(name, returnType, methodType).also {
        it.isConst = isConst
        it.isVirtual = method.isVirtual()
        it.addArgs(method.asFunctionDecl())
    }
}

private fun buildFunction(function: FunctionDecl): WrappedMethod {
    val name = function.asNamedDecl().getNameAsString() ?: error("function without a name")
    val returnType = WrappedType(function.getReturnType().getAsString() ?: error("no return"))
    return WrappedMethod(name, returnType, MethodType.STATIC).also {
        it.addArgs(function)
    }
}

private fun WrappedMethod.addArgs(function: FunctionDecl) {
    for (i in 0u until function.getNumParams()) {
        // getParamDecl returns the Api interface (the generated related-object-getter
        // convention); re-wrap to the concrete ParmVarDecl so the upcast chain resolves.
        val param = function.getParamDecl(i)?.let { ParmVarDecl(it.ptr, function.memScope) }
            ?: continue
        // Unnamed parameter (e.g. `void freeFunction(int)`) falls back to the same
        // positional name the libclang front-end synthesizes.
        val name = param.asNamedDecl().getNameAsString()?.takeIf { it.isNotBlank() }
            ?: "_arg_$i"
        val spelling = param.asValueDecl().getType().getAsString() ?: continue
        // brick-3+: hasDefault/defaultValue (ParmVarDecl::hasDefaultArg + source text).
        addChild(WrappedArgument(name, WrappedType(spelling), param.asDecl().cppUsr()))
    }
}
