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
import clang.CXXRecordDecl
import clang.QualType
import clang.Type
import clang.tooling.buildASTFromCode
import kotlinx.cinterop.memScoped

// Stage1 self-bootstrap, probe 4/4: the QualType/Type STRUCTURAL DECODE group. The prior
// bricks proved EXTRACT (names/type-strings), TRAVERSE (decls/methods/bases) and IDENTITY
// (Decl::getID()). This walk proves the fourth-and-largest primitive: reproducing — on the
// kplusplus-generated bindings of Clang's own C++ AST — everything the libclang reducer
// decodes from a CXType, plus the headline result that getCanonicalType() replaces the
// reducer's ~200-line typedef-reducer (canonical types are first-class in the C++ AST).

// QualType::getTypePtr() is declared to return the TypeApi *interface* (the generated
// accessor convention for related-object getters); the runtime object is a concrete Type,
// re-wrap it so the shape-classification methods (which live on the concrete class) resolve.
private fun QualType.typePtr(): Type? =
    getTypePtrOrNull()?.let { Type(it.ptr, memScope) }

// Type::getAsCXXRecordDecl() likewise returns the Api interface; re-wrap to the concrete
// CXXRecordDecl so getID()/asNamedDecl() (the brick-5 identity + EXTRACT) resolve.
private fun Type.asRecord(): CXXRecordDecl? =
    getAsCXXRecordDecl()?.let { CXXRecordDecl(it.ptr, memScope) }

// Self-checking verdict accumulator.
private val verdict = StringBuilder()
private var allPass = true

private fun check(name: String, pass: Boolean, detail: String = "") {
    if (!pass) allPass = false
    verdict.append("  [${if (pass) "PASS" else "FAIL"}] $name")
    if (detail.isNotEmpty()) verdict.append(" — $detail")
    verdict.append('\n')
}

// Decode + print the structure of a QualType the way the reducer reads a CXType, and return
// the canonical-type record id (if any) for identity cross-checks.
private fun decode(label: String, qt: QualType): Long? {
    val spelling = qt.getAsString() ?: "?"
    val canonical = qt.getCanonicalType()
    val canonicalSpelling = canonical.getAsString() ?: "?"
    val ty = qt.typePtr()
    val canonTy = canonical.typePtr()
    println("  $label : '$spelling'")
    println("      const=${qt.isConstQualified()} unqualified='${qt.getUnqualifiedType().getAsString()}'")
    println("      canonical='$canonicalSpelling'  (sugar-collapsed=${spelling != canonicalSpelling})")
    if (ty != null) {
        println(
            "      shape: ptr=${ty.isPointerType()} ref=${ty.isReferenceType()} " +
                "record=${ty.isRecordType()} builtin=${ty.isBuiltinType()} " +
                "enum=${ty.isEnumeralType()} array=${ty.isConstantArrayType()}",
        )
    }
    // Pointer/reference pointee (decode one level in).
    val shape = canonTy ?: ty
    if (shape != null && (shape.isPointerType() || shape.isReferenceType())) {
        val pointee = shape.getPointeeType()
        println("      pointee='${pointee.getAsString()}'")
    }
    // Record/enum decl recovery off the canonical type (this is how a type keys into the
    // reducer's elementLookup): get the referenced CXXRecordDecl and its identity.
    val recId = canonTy?.asRecord()?.let { rec ->
        val rName = rec.asNamedDecl().getNameAsString()
        val id = rec.getID()
        println("      -> CXXRecordDecl '$rName' id=$id")
        id
    }
    return recId
}

fun main(args: Array<String>) = memScoped {
    val source = """
        typedef int MyInt;
        struct Point { int x; int y; };
        struct Widget { int id; };
        using PointAlias = Point;
        template <class T> struct Holder { T value; };
        struct Shape {
            int area() const;
            const char* name() const;
            Point makePoint(const Point& p, Widget* w) const;
            MyInt counter;
            Holder<Widget*> items;
            int buffer[4];
        };
        enum Color { RED, GREEN };
        Color globalColor;
        PointAlias globalAlias;
    """.trimIndent()

    val unit = buildASTFromCode(source, "input.cc")
        ?: return@memScoped println("clangwalk: buildASTFromCode returned null")
    val context = unit.getASTContext()
        ?: return@memScoped println("clangwalk: no ASTContext")
    val tu = context.getTranslationUnitDecl()
        ?: return@memScoped println("clangwalk: no TranslationUnitDecl")

    // Pass 1: collect the top-level record identities (brick-5 getID()) so the type-decode
    // can prove a referenced-decl id round-trips to the SAME id (closing type->elementLookup).
    val recordIds = mutableMapOf<String, Long>()
    var shape: CXXRecordDecl? = null
    val globals = mutableListOf<Pair<String, QualType>>()
    for (decl in tu.asDeclContext().decls()) {
        if (decl == null) continue
        val named = decl.asNamedDecl()
        val name = named?.getNameAsString() ?: continue
        val record = decl.asCXXRecordDecl()
        if (record != null) {
            recordIds[name] = record.getID()
            if (name == "Shape") shape = record
        } else {
            // Top-level VarDecl (globalColor / globalAlias) carries a type to decode.
            val vd = decl.asValueDecl()
            if (vd != null && record == null && decl.asCXXMethodDecl() == null) {
                globals += name to vd.getType()
            }
        }
    }
    println("clangwalk: top-level record ids = $recordIds")
    val pointId = recordIds["Point"]
    val widgetId = recordIds["Widget"]

    val theShape = shape ?: return@memScoped println("clangwalk: no Shape record")

    // ---- Decode the Shape members ----
    println("\nclangwalk: decode Shape fields")
    // Field types via the record's DeclContext (FieldDecl is-a ValueDecl).
    var counterCanonicalIsBuiltin = false
    var bufferIsArray = false
    var itemsRecordId: Long? = null
    var itemsArgPointeeId: Long? = null
    for (member in theShape.asDeclContext().decls()) {
        if (member == null) continue
        if (member.asCXXMethodDecl() != null) continue
        val vd = member.asValueDecl() ?: continue
        val mName = member.asNamedDecl()?.getNameAsString() ?: "<unnamed>"
        val qt = vd.getType()
        val recId = decode("field $mName", qt)
        when (mName) {
            "counter" -> counterCanonicalIsBuiltin = qt.getCanonicalType().typePtr()?.isBuiltinType() == true
            "buffer" -> bufferIsArray = qt.typePtr()?.isConstantArrayType() == true
            "items" -> {
                itemsRecordId = recId
                // Template-specialization decode: the instantiation's record dyn_casts to
                // ClassTemplateSpecializationDecl -> getTemplateArgs() -> arg 0 element type.
                val spec = qt.getCanonicalType().typePtr()?.asRecord()
                    ?.asClassTemplateSpecializationDecl()
                val argList = spec?.getTemplateArgs()
                if (argList != null && argList.size() > 0u) {
                    val arg0 = argList.get(0u)
                    val argType = arg0?.getAsType()
                    println("      <template arg0> kind=${arg0?.getKind()} type='${argType?.getAsString()}'")
                    // arg0 is Widget* — decode its pointee record identity.
                    val pointee = argType?.getCanonicalType()?.typePtr()
                        ?.takeIf { it.isPointerType() }?.getPointeeType()
                    itemsArgPointeeId = pointee?.getCanonicalType()?.typePtr()?.asRecord()?.getID()
                    println("      <template arg0 pointee record id> $itemsArgPointeeId")
                }
            }
        }
    }

    // ---- Decode makePoint via FunctionProtoType (primitive 4) ----
    println("\nclangwalk: decode Shape::makePoint via FunctionProtoType")
    var protoReturnId: Long? = null
    var protoNumParams = -1
    var protoParam0Ref = false
    var protoParam1Ptr = false
    for (method in theShape.methods()) {
        if (method == null) continue
        val mName = method.asNamedDecl()?.getNameAsString() ?: continue
        if (mName != "makePoint") continue
        val proto = method.getType().typePtr()?.asFunctionProtoType()
        if (proto == null) {
            println("  makePoint: asFunctionProtoType() returned null (dyn_cast gap)")
            break
        }
        protoNumParams = proto.getNumParams().toInt()
        val ret = proto.getReturnType()
        println("  return='${ret.getAsString()}' numParams=$protoNumParams")
        protoReturnId = ret.getCanonicalType().typePtr()?.asRecord()?.getID()
        for (i in 0 until protoNumParams) {
            val p = proto.getParamType(i.toUInt())
            val pTy = p.getCanonicalType().typePtr()
            println("  param[$i]='${p.getAsString()}' ref=${pTy?.isReferenceType()} ptr=${pTy?.isPointerType()}")
            if (i == 0) protoParam0Ref = pTy?.isReferenceType() == true
            if (i == 1) protoParam1Ptr = pTy?.isPointerType() == true
        }
    }

    // ---- Decode the globals (typedef-alias canonical collapse) ----
    println("\nclangwalk: decode top-level globals")
    var aliasRecordId: Long? = null
    var colorIsEnum = false
    for ((gName, gType) in globals) {
        val recId = decode("global $gName", gType)
        when (gName) {
            "globalAlias" -> aliasRecordId = recId
            "globalColor" -> colorIsEnum = gType.typePtr()?.isEnumeralType() == true
        }
    }

    // ================= VERDICT =================
    println("\n================ TYPE-DECODE VERDICT ================")
    // P1: canonical collapse — the typedef-reducer replacement.
    check(
        "P1 alias 'PointAlias' canonical collapses to underlying record 'Point'",
        aliasRecordId != null && aliasRecordId == pointId,
        "aliasRecordId=$aliasRecordId pointId=$pointId",
    )
    check(
        "P1 typedef 'MyInt' canonical is the builtin 'int'",
        counterCanonicalIsBuiltin,
    )
    // P2: shape classification.
    check("P2 'buffer' classifies as constant array", bufferIsArray)
    check("P2 'globalColor' classifies as enumeral", colorIsEnum)
    // P3: record type -> referenced decl identity round-trips to brick-5 top-level id.
    check(
        "P3 makePoint return record id == top-level Point id",
        protoReturnId != null && protoReturnId == pointId,
        "returnId=$protoReturnId pointId=$pointId",
    )
    // P4: function-proto decode.
    check("P4 FunctionProtoType getNumParams() == 2", protoNumParams == 2)
    check("P4 param[0] (const Point&) is a reference", protoParam0Ref)
    check("P4 param[1] (Widget*) is a pointer", protoParam1Ptr)
    // P5: template-specialization argument decode + identity.
    check(
        "P5 'items' record id == top-level Holder spec (non-null)",
        itemsRecordId != null,
        "itemsRecordId=$itemsRecordId",
    )
    check(
        "P5 template arg0 pointee record id == top-level Widget id",
        itemsArgPointeeId != null && itemsArgPointeeId == widgetId,
        "argPointeeId=$itemsArgPointeeId widgetId=$widgetId",
    )
    print(verdict)
    println("==================================================")
    println("clangwalk: TYPE-DECODE PROBE ${if (allPass) "ALL PASS" else "HAD FAILURES"}")
}
