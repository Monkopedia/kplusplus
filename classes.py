#!/usr/bin/python

import clang
import clang.cindex as cl
import json
import sys

from clang.cindex import CursorKind

def fully_qualified(c):
    if c is None:
        return ''
    elif c.kind == CursorKind.TRANSLATION_UNIT:
        return ''
    else:
        res = fully_qualified(c.semantic_parent)
        if res != '': 
            return res+"::"+c.spelling
        return c.spelling 

idx = clang.cindex.Index.create()
tu = idx.parse(sys.argv[1], args='-xc++ --std=c++11'.split())
output = []
for c in tu.cursor.walk_preorder():
    if c.kind == CursorKind.CLASS_DECL and not fully_qualified(c).startswith('std::'):
        cls = {}
        cls["name"] = fully_qualified(c)
        types = []
        for i in range(c.get_num_template_arguments()):
            types.append({"name": c.get_template_argument_value(i).spelling, "type": fully_qualified(c.get_template_argument_type(i)) })
        cls["types"] = types
        methods = []
        fields = []
        for m in c.referenced.get_children():
            if m.kind == CursorKind.CXX_BASE_SPECIFIER:
                cls["base"] = fully_qualified(m.referenced)
            elif m.kind == CursorKind.FIELD_DECL:
                field = {}
                # print('{')

                field["name"] = fully_qualified(m.referenced)
                field["kind"] = str(m.kind.name)
                field["access"] = str(m.access_specifier.name)
                field["mutable"] = str(m.is_mutable_field())
                field["static"] = str(m.is_static_method())
                field["type"] = m.type.spelling
                fields.append(field)
            elif m.kind == CursorKind.CXX_METHOD or m.kind == CursorKind.CONSTRUCTOR or m.kind == CursorKind.DESTRUCTOR:
                method = {}
                # print('{')

                method["name"] = fully_qualified(m.referenced)
                method["kind"] = str(m.kind.name)
                method["access"] = str(m.access_specifier.name)
                method["static"] = str(m.is_static_method())
                method["isCopyConstructor"] = str(m.is_copy_constructor())
                method["isDefaultConstructor"] = str(m.is_default_constructor())
                method["isMoveConstructor"] = str(m.is_move_constructor())
                method["isDefaultMethod"] = str(m.is_default_method())
                method["isConstMethod"] = str(m.is_const_method())
                method["isVirtualMethod"] = str(m.is_virtual_method())

                types = []
                for i in range(m.get_num_template_arguments()):
                    types.append({"name": m.get_template_argument_value(i).spelling, "type": fully_qualified(m.get_template_argument_type(i)) })
                method["types"] = types
                args = []
                for a in m.get_arguments():
                    args.append({"name": a.spelling, "type": a.type.spelling})
                method["args"] = args
                method["return"] = m.type.get_result().spelling
                methods.append(method)
        cls["methods"] = methods
        cls["fields"] = fields
        output.append(cls)
y = json.dumps(output)
print(y)
