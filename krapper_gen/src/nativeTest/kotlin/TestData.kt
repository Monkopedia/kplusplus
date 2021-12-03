/*
 * Copyright 2021 Jason Monk
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
package com.monkopedia.krapper.generator

import com.monkopedia.krapper.generator.model.MethodType
import com.monkopedia.krapper.generator.model.WrappedArgument
import com.monkopedia.krapper.generator.model.WrappedClass
import com.monkopedia.krapper.generator.model.WrappedConstructor
import com.monkopedia.krapper.generator.model.WrappedDestructor
import com.monkopedia.krapper.generator.model.WrappedField
import com.monkopedia.krapper.generator.model.WrappedMethod
import com.monkopedia.krapper.generator.model.WrappedNamespace
import com.monkopedia.krapper.generator.model.WrappedTU
import com.monkopedia.krapper.generator.model.WrappedTemplate
import com.monkopedia.krapper.generator.model.WrappedTemplateParam
import com.monkopedia.krapper.generator.model.type.WrappedTemplateRef
import com.monkopedia.krapper.generator.model.type.WrappedTemplateType
import com.monkopedia.krapper.generator.model.type.WrappedType
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.VOID
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.const
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.pointerTo
import com.monkopedia.krapper.generator.model.type.WrappedType.Companion.referenceTo

@ThreadLocal
val TestData by lazy {
    TestDataClass()
}

class TestDataClass {

    val HEADER = """
        #pragma once

        #include <iostream>
        #include <vector>
        #include <string>

        namespace TestLib {


        class OtherClass {
            private:
                std::string privateString;
            public:
                std::string getPrivateString();
                void setPrivateString(std::string value);
                void appendText(std::vector<std::string> text);
        };

        class TestClass {
            private:
                std::string privateString;
            public:
                bool b;
                size_t st;
                uint16_t uit;

                int array[5];
                char* str;

                signed char c;
                unsigned char uc;
                signed short s;
                unsigned short us;
                signed int i;
                unsigned int ui;
                signed long l;
                unsigned long ul;
                signed long long ll;
                unsigned long long ull;
                float f;
                double d;
                long double ld;

                bool *pb;
                signed char *pc;
                unsigned char *puc;
                signed short *ps;
                unsigned short *pus;
                signed int *pi;
                unsigned int *pui;
                signed long *pl;
                unsigned long *pul;
                signed long long *pll;
                unsigned long long *pull;
                float *pf;
                double *pd;

                TestClass();
                TestClass(const TestClass &other);
                TestClass(int a);
                TestClass(int a, double b);

                long sum();
                long* longPointer();
                void setSome(int a, long b, long long c);
                void setPointers(int* a, long* b, long long* c);
                void setPrivateString(std::string value);
                void setPrivateFrom(OtherClass* other);
         
                void output();

                // Operator overloading
                TestClass operator-(TestClass c2);
                TestClass operator-();
                TestClass operator+(TestClass c2);
                TestClass operator+();
                TestClass operator*(TestClass c2);
                TestClass operator/(TestClass c2);
                TestClass operator%(TestClass c2);
                TestClass operator++();
                TestClass operator++(int dummy);
                TestClass operator--();
                TestClass operator--(int dummy);
                TestClass operator==(TestClass &c2);
                TestClass operator!=(TestClass &c2);
                TestClass operator<(TestClass &c2);
                TestClass operator>(TestClass &c2);
                TestClass operator<=(TestClass &c2);
                TestClass operator>=(TestClass &c2);
                TestClass operator!();
                TestClass operator&&(TestClass &c2);
                TestClass operator||(TestClass &c2);
                TestClass operator~();
                TestClass operator&(TestClass &c2);
                TestClass operator|(TestClass &c2);
                TestClass operator^(TestClass &c2);
                TestClass operator<<(TestClass &c2);
                TestClass operator>>(TestClass &c2);
                TestClass operator[](std::string &c2);
        };
        }
    """.trimIndent()

    val JSON = """
        [
          {
            "fullyQualified": "TestLib::OtherClass",
            "methods": [
              {
                "name": "getPrivateString",
                "returnType": {
                  "name": "std::string"
                },
                "args": [],
                "isStatic": false,
                "methodType": "METHOD"
              },
              {
                "name": "setPrivateString",
                "returnType": {
                  "name": "void"
                },
                "args": [
                  {
                    "name": "value",
                    "type": {
                      "name": "std::string"
                    }
                  }
                ],
                "isStatic": false,
                "methodType": "METHOD"
              },
              {
                "name": "appendText",
                "returnType": {
                  "name": "void"
                },
                "args": [
                  {
                    "name": "text",
                    "type": {
                      "name": "std::vector<std::string>"
                    }
                  }
                ],
                "isStatic": false,
                "methodType": "METHOD"
              }
            ]
          },
          {
            "fullyQualified": "TestLib::TestClass",
            "fields": [
              {
                "name": "b",
                "type": {
                  "name": "bool"
                }
              },
              {
                "name": "st",
                "type": {
                  "name": "size_t"
                }
              },
              {
                "name": "uit",
                "type": {
                  "name": "uint16_t"
                }
              },
              {
                "name": "array",
                "type": {
                  "name": "int [5]"
                }
              },
              {
                "name": "str",
                "type": {
                  "name": "char *"
                }
              },
              {
                "name": "c",
                "type": {
                  "name": "signed char"
                }
              },
              {
                "name": "uc",
                "type": {
                  "name": "unsigned char"
                }
              },
              {
                "name": "s",
                "type": {
                  "name": "short"
                }
              },
              {
                "name": "us",
                "type": {
                  "name": "unsigned short"
                }
              },
              {
                "name": "i",
                "type": {
                  "name": "int"
                }
              },
              {
                "name": "ui",
                "type": {
                  "name": "unsigned int"
                }
              },
              {
                "name": "l",
                "type": {
                  "name": "long"
                }
              },
              {
                "name": "ul",
                "type": {
                  "name": "unsigned long"
                }
              },
              {
                "name": "ll",
                "type": {
                  "name": "long long"
                }
              },
              {
                "name": "ull",
                "type": {
                  "name": "unsigned long long"
                }
              },
              {
                "name": "f",
                "type": {
                  "name": "float"
                }
              },
              {
                "name": "d",
                "type": {
                  "name": "double"
                }
              },
              {
                "name": "ld",
                "type": {
                  "name": "long double"
                }
              },
              {
                "name": "pb",
                "type": {
                  "name": "bool *"
                }
              },
              {
                "name": "pc",
                "type": {
                  "name": "signed char *"
                }
              },
              {
                "name": "puc",
                "type": {
                  "name": "unsigned char *"
                }
              },
              {
                "name": "ps",
                "type": {
                  "name": "short *"
                }
              },
              {
                "name": "pus",
                "type": {
                  "name": "unsigned short *"
                }
              },
              {
                "name": "pi",
                "type": {
                  "name": "int *"
                }
              },
              {
                "name": "pui",
                "type": {
                  "name": "unsigned int *"
                }
              },
              {
                "name": "pl",
                "type": {
                  "name": "long *"
                }
              },
              {
                "name": "pul",
                "type": {
                  "name": "unsigned long *"
                }
              },
              {
                "name": "pll",
                "type": {
                  "name": "long long *"
                }
              },
              {
                "name": "pull",
                "type": {
                  "name": "unsigned long long *"
                }
              },
              {
                "name": "pf",
                "type": {
                  "name": "float *"
                }
              },
              {
                "name": "pd",
                "type": {
                  "name": "double *"
                }
              }
            ],
            "methods": [
              {
                "name": "TestClass",
                "returnType": {
                  "name": "void"
                },
                "args": [],
                "isStatic": false,
                "methodType": "CONSTRUCTOR"
              },
              {
                "name": "TestClass",
                "returnType": {
                  "name": "void"
                },
                "args": [
                  {
                    "name": "other",
                    "type": {
                      "name": "const TestLib::TestClass &"
                    }
                  }
                ],
                "isStatic": false,
                "methodType": "CONSTRUCTOR"
              },
              {
                "name": "TestClass",
                "returnType": {
                  "name": "void"
                },
                "args": [
                  {
                    "name": "a",
                    "type": {
                      "name": "int"
                    }
                  }
                ],
                "isStatic": false,
                "methodType": "CONSTRUCTOR"
              },
              {
                "name": "TestClass",
                "returnType": {
                  "name": "void"
                },
                "args": [
                  {
                    "name": "a",
                    "type": {
                      "name": "int"
                    }
                  },
                  {
                    "name": "b",
                    "type": {
                      "name": "double"
                    }
                  }
                ],
                "isStatic": false,
                "methodType": "CONSTRUCTOR"
              },
              {
                "name": "sum",
                "returnType": {
                  "name": "long"
                },
                "args": [],
                "isStatic": false,
                "methodType": "METHOD"
              },
              {
                "name": "longPointer",
                "returnType": {
                  "name": "long *"
                },
                "args": [],
                "isStatic": false,
                "methodType": "METHOD"
              },
              {
                "name": "setSome",
                "returnType": {
                  "name": "void"
                },
                "args": [
                  {
                    "name": "a",
                    "type": {
                      "name": "int"
                    }
                  },
                  {
                    "name": "b",
                    "type": {
                      "name": "long"
                    }
                  },
                  {
                    "name": "c",
                    "type": {
                      "name": "long long"
                    }
                  }
                ],
                "isStatic": false,
                "methodType": "METHOD"
              },
              {
                "name": "setPointers",
                "returnType": {
                  "name": "void"
                },
                "args": [
                  {
                    "name": "a",
                    "type": {
                      "name": "int *"
                    }
                  },
                  {
                    "name": "b",
                    "type": {
                      "name": "long *"
                    }
                  },
                  {
                    "name": "c",
                    "type": {
                      "name": "long long *"
                    }
                  }
                ],
                "isStatic": false,
                "methodType": "METHOD"
              },
              {
                "name": "setPrivateString",
                "returnType": {
                  "name": "void"
                },
                "args": [
                  {
                    "name": "value",
                    "type": {
                      "name": "std::string"
                    }
                  }
                ],
                "isStatic": false,
                "methodType": "METHOD"
              },
              {
                "name": "setPrivateFrom",
                "returnType": {
                  "name": "void"
                },
                "args": [
                  {
                    "name": "other",
                    "type": {
                      "name": "TestLib::OtherClass *"
                    }
                  }
                ],
                "isStatic": false,
                "methodType": "METHOD"
              },
              {
                "name": "output",
                "returnType": {
                  "name": "void"
                },
                "args": [],
                "isStatic": false,
                "methodType": "METHOD"
              },
              {
                "name": "operator-",
                "returnType": {
                  "name": "TestLib::TestClass"
                },
                "args": [
                  {
                    "name": "c2",
                    "type": {
                      "name": "TestLib::TestClass"
                    }
                  }
                ],
                "isStatic": false,
                "methodType": "METHOD"
              },
              {
                "name": "operator-",
                "returnType": {
                  "name": "TestLib::TestClass"
                },
                "args": [],
                "isStatic": false,
                "methodType": "METHOD"
              },
              {
                "name": "operator+",
                "returnType": {
                  "name": "TestLib::TestClass"
                },
                "args": [
                  {
                    "name": "c2",
                    "type": {
                      "name": "TestLib::TestClass"
                    }
                  }
                ],
                "isStatic": false,
                "methodType": "METHOD"
              },
              {
                "name": "operator+",
                "returnType": {
                  "name": "TestLib::TestClass"
                },
                "args": [],
                "isStatic": false,
                "methodType": "METHOD"
              },
              {
                "name": "operator*",
                "returnType": {
                  "name": "TestLib::TestClass"
                },
                "args": [
                  {
                    "name": "c2",
                    "type": {
                      "name": "TestLib::TestClass"
                    }
                  }
                ],
                "isStatic": false,
                "methodType": "METHOD"
              },
              {
                "name": "operator/",
                "returnType": {
                  "name": "TestLib::TestClass"
                },
                "args": [
                  {
                    "name": "c2",
                    "type": {
                      "name": "TestLib::TestClass"
                    }
                  }
                ],
                "isStatic": false,
                "methodType": "METHOD"
              },
              {
                "name": "operator%",
                "returnType": {
                  "name": "TestLib::TestClass"
                },
                "args": [
                  {
                    "name": "c2",
                    "type": {
                      "name": "TestLib::TestClass"
                    }
                  }
                ],
                "isStatic": false,
                "methodType": "METHOD"
              },
              {
                "name": "operator++",
                "returnType": {
                  "name": "TestLib::TestClass"
                },
                "args": [],
                "isStatic": false,
                "methodType": "METHOD"
              },
              {
                "name": "operator++",
                "returnType": {
                  "name": "TestLib::TestClass"
                },
                "args": [
                  {
                    "name": "dummy",
                    "type": {
                      "name": "int"
                    }
                  }
                ],
                "isStatic": false,
                "methodType": "METHOD"
              },
              {
                "name": "operator--",
                "returnType": {
                  "name": "TestLib::TestClass"
                },
                "args": [],
                "isStatic": false,
                "methodType": "METHOD"
              },
              {
                "name": "operator--",
                "returnType": {
                  "name": "TestLib::TestClass"
                },
                "args": [
                  {
                    "name": "dummy",
                    "type": {
                      "name": "int"
                    }
                  }
                ],
                "isStatic": false,
                "methodType": "METHOD"
              },
              {
                "name": "operator==",
                "returnType": {
                  "name": "TestLib::TestClass"
                },
                "args": [
                  {
                    "name": "c2",
                    "type": {
                      "name": "TestLib::TestClass &"
                    }
                  }
                ],
                "isStatic": false,
                "methodType": "METHOD"
              },
              {
                "name": "operator!=",
                "returnType": {
                  "name": "TestLib::TestClass"
                },
                "args": [
                  {
                    "name": "c2",
                    "type": {
                      "name": "TestLib::TestClass &"
                    }
                  }
                ],
                "isStatic": false,
                "methodType": "METHOD"
              },
              {
                "name": "operator<",
                "returnType": {
                  "name": "TestLib::TestClass"
                },
                "args": [
                  {
                    "name": "c2",
                    "type": {
                      "name": "TestLib::TestClass &"
                    }
                  }
                ],
                "isStatic": false,
                "methodType": "METHOD"
              },
              {
                "name": "operator>",
                "returnType": {
                  "name": "TestLib::TestClass"
                },
                "args": [
                  {
                    "name": "c2",
                    "type": {
                      "name": "TestLib::TestClass &"
                    }
                  }
                ],
                "isStatic": false,
                "methodType": "METHOD"
              },
              {
                "name": "operator<=",
                "returnType": {
                  "name": "TestLib::TestClass"
                },
                "args": [
                  {
                    "name": "c2",
                    "type": {
                      "name": "TestLib::TestClass &"
                    }
                  }
                ],
                "isStatic": false,
                "methodType": "METHOD"
              },
              {
                "name": "operator>=",
                "returnType": {
                  "name": "TestLib::TestClass"
                },
                "args": [
                  {
                    "name": "c2",
                    "type": {
                      "name": "TestLib::TestClass &"
                    }
                  }
                ],
                "isStatic": false,
                "methodType": "METHOD"
              },
              {
                "name": "operator!",
                "returnType": {
                  "name": "TestLib::TestClass"
                },
                "args": [],
                "isStatic": false,
                "methodType": "METHOD"
              },
              {
                "name": "operator&&",
                "returnType": {
                  "name": "TestLib::TestClass"
                },
                "args": [
                  {
                    "name": "c2",
                    "type": {
                      "name": "TestLib::TestClass &"
                    }
                  }
                ],
                "isStatic": false,
                "methodType": "METHOD"
              },
              {
                "name": "operator||",
                "returnType": {
                  "name": "TestLib::TestClass"
                },
                "args": [
                  {
                    "name": "c2",
                    "type": {
                      "name": "TestLib::TestClass &"
                    }
                  }
                ],
                "isStatic": false,
                "methodType": "METHOD"
              },
              {
                "name": "operator~",
                "returnType": {
                  "name": "TestLib::TestClass"
                },
                "args": [],
                "isStatic": false,
                "methodType": "METHOD"
              },
              {
                "name": "operator&",
                "returnType": {
                  "name": "TestLib::TestClass"
                },
                "args": [
                  {
                    "name": "c2",
                    "type": {
                      "name": "TestLib::TestClass &"
                    }
                  }
                ],
                "isStatic": false,
                "methodType": "METHOD"
              },
              {
                "name": "operator|",
                "returnType": {
                  "name": "TestLib::TestClass"
                },
                "args": [
                  {
                    "name": "c2",
                    "type": {
                      "name": "TestLib::TestClass &"
                    }
                  }
                ],
                "isStatic": false,
                "methodType": "METHOD"
              },
              {
                "name": "operator^",
                "returnType": {
                  "name": "TestLib::TestClass"
                },
                "args": [
                  {
                    "name": "c2",
                    "type": {
                      "name": "TestLib::TestClass &"
                    }
                  }
                ],
                "isStatic": false,
                "methodType": "METHOD"
              },
              {
                "name": "operator<<",
                "returnType": {
                  "name": "TestLib::TestClass"
                },
                "args": [
                  {
                    "name": "c2",
                    "type": {
                      "name": "TestLib::TestClass &"
                    }
                  }
                ],
                "isStatic": false,
                "methodType": "METHOD"
              },
              {
                "name": "operator>>",
                "returnType": {
                  "name": "TestLib::TestClass"
                },
                "args": [
                  {
                    "name": "c2",
                    "type": {
                      "name": "TestLib::TestClass &"
                    }
                  }
                ],
                "isStatic": false,
                "methodType": "METHOD"
              },
              {
                "name": "operator[]",
                "returnType": {
                  "name": "TestLib::TestClass"
                },
                "args": [
                  {
                    "name": "c2",
                    "type": {
                      "name": "std::string &"
                    }
                  }
                ],
                "isStatic": false,
                "methodType": "METHOD"
              }
            ]
          }
        ]
    """.trimIndent()

    val TEMPLATE_HEADER = """
        #pragma once
        
        #include <iostream>
        #include <vector>
        #include <string>
        
        namespace TestLib {
        
        template <class T>
        class MyPair {
          public:
            T a, b;
            MyPair (T first, T second) {a=first; b=second;}
            T getMax();
        };
        
        template <class T>
        T MyPair<T>::getMax ()
        {
          T retval;
          retval = a>b? a : b;
          return retval;
        }
        
        class ZZ {
          public:
            MyPair<int> intPair;
            MyPair<std::string> stringPair;
            MyPair<std::vector<int>> vectorPair;
        };
        
        }
    """.trimIndent()

    val TU = WrappedTU()
    val TestLib = WrappedNamespace("TestLib").also {
        TU.addChild(it)
        it.parent = TU
    }
    val Std = WrappedNamespace("std").also {
        TU.addChild(it)
        it.parent = TU
    }

    val Vector = VectorClass()

    inner class VectorClass {
        val localType = WrappedType("std::vector")
        val template = WrappedTemplateParam("_Tp", "_Tp")
        val type = WrappedTemplateType(localType, listOf(WrappedType("std::string")))
        val constructor = WrappedConstructor("std::vector", WrappedTemplateType(localType, listOf(WrappedTemplateRef(template.usr))))
        val destructor = WrappedDestructor("~std::vector", VOID)
        val pushBack =
            WrappedMethod(
                "push_back",
                WrappedType("void"),
                false,
                MethodType.METHOD
            ).also {
                it.addChild(WrappedArgument("str", WrappedTemplateRef(template.usr)))
            }
        val tmp =
            WrappedTemplate(
                "vector",
            ).also {
                it.parent = Std
                Std.addChild(it)
                it.addAllChildren(
                    listOf(
                        template,
                        constructor,
                        destructor,
                        pushBack
                    )
                )
            }
        val cls = tmp.typedAs(type)
    }

    val TestClass = TestClassClass()

    inner class TestClassClass {
        val type = WrappedType("TestLib::TestClass")
        val b = WrappedField("b", WrappedType("bool"))
        val st = WrappedField("st", WrappedType("size_t"))
        val uit = WrappedField("uit", WrappedType("uint16_t"))
        val array = WrappedField("array", WrappedType("int [5]"))
        val str = WrappedField("str", WrappedType("std::string"))
        val c = WrappedField("c", WrappedType("signed char"))
        val uc = WrappedField("uc", WrappedType("unsigned char"))
        val s = WrappedField("s", WrappedType("signed short"))
        val us = WrappedField("us", WrappedType("unsigned short"))
        val i = WrappedField("i", WrappedType("signed int"))
        val ui = WrappedField("ui", WrappedType("unsigned int"))
        val l = WrappedField("l", WrappedType("signed long"))
        val ul = WrappedField("ul", WrappedType("unsigned long"))
        val ll = WrappedField("ll", WrappedType("signed long long"))
        val ull = WrappedField("ull", WrappedType("unsigned long long"))
        val f = WrappedField("f", WrappedType("float"))
        val d = WrappedField("d", WrappedType("double"))
        val ld = WrappedField("ld", WrappedType("long double"))
        val pb = WrappedField("pb", WrappedType("bool *"))
        val pc = WrappedField("pc", WrappedType("signed char *"))
        val puc = WrappedField("puc", WrappedType("unsigned char *"))
        val ps = WrappedField("ps", WrappedType("signed short *"))
        val pus = WrappedField("pus", WrappedType("unsigned short *"))
        val pi = WrappedField("pi", WrappedType("signed int *"))
        val pui = WrappedField("pui", WrappedType("unsigned int *"))
        val pl = WrappedField("pl", WrappedType("signed long *"))
        val pul = WrappedField("pul", WrappedType("unsigned long *"))
        val pll = WrappedField("pll", WrappedType("signed long long *"))
        val pull = WrappedField("pull", WrappedType("unsigned long long *"))
        val pf = WrappedField("pf", WrappedType("float *"))
        val pd = WrappedField("pd", WrappedType("double *"))

        val sum =
            WrappedMethod("sum", WrappedType("long"), false, MethodType.METHOD)
        val longPointer = WrappedMethod(
            "longPointer",
            WrappedType("long *"),
            false,
            MethodType.METHOD,
        )
        val setSome =
            WrappedMethod(
                "setSome",
                WrappedType("void"),
                false,
                MethodType.METHOD,
            ).also {
                it.addChild(WrappedArgument("a", WrappedType("int")))
                it.addChild(WrappedArgument("b", WrappedType("long")))
                it.addChild(WrappedArgument("c", WrappedType("long long")))
            }
        val setPointers =
            WrappedMethod(
                "setPointers",
                WrappedType("void"),
                false,
                MethodType.METHOD,
            ).also {
                it.addAllChildren(
                    listOf(
                        WrappedArgument("a", WrappedType("int*")),
                        WrappedArgument("b", WrappedType("long*")),
                        WrappedArgument("c", WrappedType("long long*"))
                    )
                )
            }
        val setPrivateString =
            WrappedMethod(
                "setPrivateString",
                WrappedType("void"),
                false,
                MethodType.METHOD,
            ).also {
                it.addAllChildren(
                    listOf(WrappedArgument("value", WrappedType("std::string"))),
                )
            }
        val setPrivateFrom =
            WrappedMethod(
                "setPrivateFrom",
                WrappedType("void"),
                false,
                MethodType.METHOD,
            ).also {
                it.addAllChildren(
                    listOf(WrappedArgument("value", WrappedType("TestLib::OtherClass *"))),
                )
            }
        val output = WrappedMethod(
            "output",
            WrappedType("void"),
            false,
            MethodType.METHOD,
        )
        val constructor = WrappedConstructor("new", type)
        val copyConstructor =
            WrappedConstructor("new", type).also {
                it.addAllChildren(listOf(WrappedArgument("other", referenceTo(const(type)))))
            }
        val otherConstructor =
            WrappedConstructor("new", type).also {
                it.addAllChildren(listOf(WrappedArgument("a", WrappedType("int"))))
            }
        val twoParamConstructor =
            WrappedConstructor("new", type).also {
                it.addAllChildren(
                    listOf(
                        WrappedArgument("a", WrappedType("int")),
                        WrappedArgument("b", WrappedType("double"))
                    ),
                )
            }
        val destructor = WrappedDestructor("~TestClass", type)
        val operatorMinus =
            WrappedMethod(
                "operator-",
                type,
                false,
                MethodType.METHOD,
            ).also {
                it.addAllChildren(
                    listOf(WrappedArgument("c2", type)),
                )
            }
        val operatorUnaryMinus =
            WrappedMethod("operator-", type, false, MethodType.METHOD)
        val operatorPlus =
            WrappedMethod(
                "operator+",
                type,
                false,
                MethodType.METHOD,
            ).also {
                it.addAllChildren(
                    listOf(WrappedArgument("c2", type)),
                )
            }
        val operatorUnaryPlus =
            WrappedMethod("operator+", type, false, MethodType.METHOD)
        val operatorTimes =
            WrappedMethod(
                "operator*",
                type,
                false,
                MethodType.METHOD,
            ).also {
                it.addAllChildren(listOf(WrappedArgument("c2", type)))
            }
        val operatorDiv = WrappedMethod(
            "operator/",
            type,
            false,
            MethodType.METHOD,
        ).also {
            it.addAllChildren(listOf(WrappedArgument("c2", type)))
        }
        val operatorRem = WrappedMethod(
            "operator%",
            type,
            false,
            MethodType.METHOD,
        ).also {
            it.addAllChildren(listOf(WrappedArgument("c2", type)))
        }
        val operatorInc = WrappedMethod("operator++", type, false, MethodType.METHOD)
        val operatorPostInc = WrappedMethod(
            "operator++",
            type,
            false,
            MethodType.METHOD,
        ).also {
            it.addAllChildren(
                listOf(WrappedArgument("dummy", WrappedType("int"))),
            )
        }
        val operatorDec = WrappedMethod("operator--", type, false, MethodType.METHOD)
        val operatorPostDec = WrappedMethod(
            "operator--",
            type,
            false,
            MethodType.METHOD,
        ).also {
            it.addAllChildren(
                listOf(WrappedArgument("dummy", WrappedType("int"))),
            )
        }
        val operatorEq = WrappedMethod(
            "operator==",
            type,
            false,
            MethodType.METHOD,
        ).also {
            it.addAllChildren(
                listOf(WrappedArgument("c2", WrappedType("TestLib::TestClass &"))),
            )
        }
        val operatorNeq = WrappedMethod(
            "operator!=",
            type,
            false,
            MethodType.METHOD,
        ).also {
            it.addAllChildren(
                listOf(WrappedArgument("c2", WrappedType("TestLib::TestClass &"))),
            )
        }
        val operatorLt = WrappedMethod(
            "operator<",
            type,
            false,
            MethodType.METHOD,
        ).also {
            it.addAllChildren(
                listOf(WrappedArgument("c2", WrappedType("TestLib::TestClass &"))),
            )
        }
        val operatorGt = WrappedMethod(
            "operator>",
            type,
            false,
            MethodType.METHOD,
        ).also {
            it.addAllChildren(
                listOf(WrappedArgument("c2", WrappedType("TestLib::TestClass &"))),
            )
        }
        val operatorLteq = WrappedMethod(
            "operator<=",
            type,
            false,
            MethodType.METHOD,
        ).also {
            it.addAllChildren(
                listOf(WrappedArgument("c2", WrappedType("TestLib::TestClass &"))),
            )
        }
        val operatorGteq = WrappedMethod(
            "operator>=",
            type,
            false,
            MethodType.METHOD,
        ).also {
            it.addAllChildren(
                listOf(WrappedArgument("c2", WrappedType("TestLib::TestClass &"))),
            )
        }
        val operatorNot =
            WrappedMethod("operator!", referenceTo(type), false, MethodType.METHOD)
        val operatorBinaryAnd = WrappedMethod(
            "operator&&",
            type,
            false,
            MethodType.METHOD,
        ).also {
            it.addAllChildren(
                listOf(WrappedArgument("c", WrappedType("TestLib::TestClass &"))),
            )
        }
        val operatorBinaryOr = WrappedMethod(
            "operator||",
            type,
            false,
            MethodType.METHOD,
        ).also {
            it.addAllChildren(
                listOf(WrappedArgument("c2", WrappedType("TestLib::TestClass &"))),
            )
        }
        val operatorInv =
            WrappedMethod("operator~", pointerTo(type), false, MethodType.METHOD)
        val operatorBitwiseAnd = WrappedMethod(
            "operator&",
            type,
            false,
            MethodType.METHOD,
        ).also {
            it.addAllChildren(
                listOf(WrappedArgument("c", WrappedType("TestLib::TestClass &"))),
            )
        }
        val operatorBitwiseOr = WrappedMethod(
            "operator|",
            type,
            false,
            MethodType.METHOD,
        ).also {
            it.addAllChildren(
                listOf(WrappedArgument("c2", WrappedType("TestLib::TestClass &"))),
            )
        }
        val operatorXor = WrappedMethod(
            "operator^",
            type,
            false,
            MethodType.METHOD,
        ).also {
            it.addAllChildren(
                listOf(WrappedArgument("c2", WrappedType("TestLib::TestClass &"))),
            )
        }
        val operatorShl = WrappedMethod(
            "operator<<",
            type,
            false,
            MethodType.METHOD,
        ).also {
            it.addAllChildren(
                listOf(WrappedArgument("c2", WrappedType("TestLib::TestClass &"))),
            )
        }
        val operatorShr = WrappedMethod(
            "operator>>",
            type,
            false,
            MethodType.METHOD,
        ).also {
            it.addAllChildren(
                listOf(WrappedArgument("c2", WrappedType("TestLib::TestClass &"))),
            )
        }
        val operatorInd = WrappedMethod(
            "operator[]",
            type,
            false,
            MethodType.METHOD,
        ).also {
            it.addAllChildren(
                listOf(WrappedArgument("c2", WrappedType("std::string &"))),
            )
        }

        val cls =
            WrappedClass(
                "TestClass",
            ).also {
                TestLib.addChild(it)
                it.parent = TestLib
                it.addAllChildren(
                    listOf(
                        b,
                        st,
                        uit,
                        array,
                        str,
                        c,
                        uc,
                        s,
                        us,
                        i,
                        ui,
                        l,
                        ul,
                        ll,
                        ull,
                        f,
                        d,
                        ld,
                        pb,
                        pc,
                        puc,
                        ps,
                        pus,
                        pi,
                        pui,
                        pl,
                        pul,
                        pll,
                        pull,
                        pf,
                        pd,
                        sum,
                        longPointer,
                        setSome,
                        setPointers,
                        setPrivateString,
                        setPrivateFrom,
                        output,
                        constructor,
                        copyConstructor,
                        otherConstructor,
                        twoParamConstructor,
                        destructor,
                        operatorMinus,
                        operatorUnaryMinus,
                        operatorPlus,
                        operatorUnaryPlus,
                        operatorTimes,
                        operatorDiv,
                        operatorRem,
                        operatorInc,
                        operatorPostInc,
                        operatorDec,
                        operatorPostDec,
                        operatorEq,
                        operatorNeq,
                        operatorLt,
                        operatorGt,
                        operatorLteq,
                        operatorGteq,
                        operatorNot,
                        operatorBinaryAnd,
                        operatorBinaryOr,
                        operatorInv,
                        operatorBitwiseAnd,
                        operatorBitwiseOr,
                        operatorXor,
                        operatorShl,
                        operatorShr,
                        operatorInd
                    )
                )
            }
    }

    val OtherClass = OtherClassClass()

    inner class OtherClassClass {
        val type = WrappedType("TestLib::OtherClass")
        val constructor = WrappedConstructor("TestLib::OtherClass", type)
        val destructor = WrappedDestructor("~TestLib::OtherClass", type)
        val getPrivateString = WrappedMethod(
            "getPrivateString",
            WrappedType("std::string"),
            false,
            MethodType.METHOD
        )
        val setPrivateString = WrappedMethod(
            "setPrivateString",
            WrappedType("void"),
            false,
            MethodType.METHOD
        ).also {
            it.addAllChildren(
                listOf(WrappedArgument("value", WrappedType("std::string"))),
            )
        }
        val appendText = WrappedMethod(
            "appendText",
            WrappedType("void"),
            false,
            MethodType.METHOD
        ).also {
            it.addAllChildren(
                listOf(
                    WrappedArgument("text", Vector.type)
                ),
            )
        }
        val copies = WrappedMethod(
            "copies",
            pointerTo(
                WrappedTemplateType(
                    WrappedType("TestLib::MyPair"),
                    listOf(pointerTo(WrappedType("TestLib::OtherClass")))
                )
            ),
            false,
            MethodType.METHOD
        )
        val ints = WrappedMethod(
            "ints",
            pointerTo(
                WrappedTemplateType(
                    WrappedType("TestLib::MyPair"),
                    listOf(WrappedType("int"))
                )
            ),
            false,
            MethodType.METHOD
        )
        val cls =
            WrappedClass(
                "OtherClass",
            ).also {
                TestLib.addChild(it)
                it.parent = TestLib
                it.addAllChildren(
                    listOf(
                        constructor,
                        destructor,
                        getPrivateString,
                        setPrivateString,
                        appendText,
                        copies,
                        ints
                    )
                )
            }
    }

    val MyPair = MyPairClass()

    inner class MyPairClass {
        val type = WrappedType("TestLib::MyPair")
        val templateParam = WrappedTemplateParam("T", "T")
        val aprop = WrappedField("a", WrappedTemplateRef(templateParam.usr))
        val bprop = WrappedField("b", WrappedTemplateRef(templateParam.usr))
        val constructor =
            WrappedConstructor("TestLib::MyPair", type).also {
                it.addAllChildren(
                    listOf(
                        WrappedArgument("first", WrappedTemplateRef(templateParam.usr)),
                        WrappedArgument("second", WrappedTemplateRef(templateParam.usr))
                    )
                )
            }
        val max = WrappedMethod(
            "getMax",
            WrappedTemplateRef(templateParam.usr),
            false
        )
        val template =
            WrappedTemplate("MyPair").also {
                TestLib.addChild(it)
                it.parent = TestLib
                it.addAllChildren(
                    listOf(
                        templateParam,
                        aprop,
                        bprop,
                        constructor,
                        max
                    )
                )
            }
        val cls = template.typedAs(WrappedTemplateType(type, listOf(pointerTo(OtherClass.type))))
    }
}
