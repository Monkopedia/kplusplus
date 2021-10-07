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
import com.monkopedia.krapper.generator.model.WrappedField
import com.monkopedia.krapper.generator.model.WrappedMethod
import com.monkopedia.krapper.generator.model.WrappedTypeReference
import com.monkopedia.krapper.generator.model.WrappedTypeReference.Companion.pointerTo
import com.monkopedia.krapper.generator.model.WrappedTypeReference.Companion.referenceTo

object TestData {

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

    object Vector {
        val type = WrappedTypeReference("std::vector<std::string>")
        val constructor = WrappedMethod(
            "std::vector<std::string>",
            type,
            emptyList(),
            false,
            MethodType.CONSTRUCTOR
        )
        val destructor = WrappedMethod(
            "~std::vector<std::string>",
            type,
            emptyList(),
            false,
            MethodType.DESTRUCTOR
        )
        val pushBack = WrappedMethod(
            "push_back",
            WrappedTypeReference("void"),
            listOf(WrappedArgument("str", WrappedTypeReference("std::string"))),
            false,
            MethodType.METHOD
        )
        val cls = WrappedClass(
            "std::vector<std::string>",
            fields = emptyList(),
            methods = listOf(
                constructor,
                destructor,
                pushBack
            )
        )
    }

    object TestClass {
        val type = WrappedTypeReference("TestLib::TestClass")
        val b = WrappedField("b", WrappedTypeReference("bool"))
        val st = WrappedField("st", WrappedTypeReference("size_t"))
        val uit = WrappedField("uit", WrappedTypeReference("uint16_t"))
        val array = WrappedField("array", WrappedTypeReference("int [5]"))
        val str = WrappedField("str", WrappedTypeReference("std::string"))
        val c = WrappedField("c", WrappedTypeReference("signed char"))
        val uc = WrappedField("uc", WrappedTypeReference("unsigned char"))
        val s = WrappedField("s", WrappedTypeReference("signed short"))
        val us = WrappedField("us", WrappedTypeReference("unsigned short"))
        val i = WrappedField("i", WrappedTypeReference("signed int"))
        val ui = WrappedField("ui", WrappedTypeReference("unsigned int"))
        val l = WrappedField("l", WrappedTypeReference("signed long"))
        val ul = WrappedField("ul", WrappedTypeReference("unsigned long"))
        val ll = WrappedField("ll", WrappedTypeReference("signed long long"))
        val ull = WrappedField("ull", WrappedTypeReference("unsigned long long"))
        val f = WrappedField("f", WrappedTypeReference("float"))
        val d = WrappedField("d", WrappedTypeReference("double"))
        val ld = WrappedField("ld", WrappedTypeReference("long double"))
        val pb = WrappedField("pb", WrappedTypeReference("bool *"))
        val pc = WrappedField("pc", WrappedTypeReference("signed char *"))
        val puc = WrappedField("puc", WrappedTypeReference("unsigned char *"))
        val ps = WrappedField("ps", WrappedTypeReference("signed short *"))
        val pus = WrappedField("pus", WrappedTypeReference("unsigned short *"))
        val pi = WrappedField("pi", WrappedTypeReference("signed int *"))
        val pui = WrappedField("pui", WrappedTypeReference("unsigned int *"))
        val pl = WrappedField("pl", WrappedTypeReference("signed long *"))
        val pul = WrappedField("pul", WrappedTypeReference("unsigned long *"))
        val pll = WrappedField("pll", WrappedTypeReference("signed long long *"))
        val pull = WrappedField("pull", WrappedTypeReference("unsigned long long *"))
        val pf = WrappedField("pf", WrappedTypeReference("float *"))
        val pd = WrappedField("pd", WrappedTypeReference("double *"))

        val sum =
            WrappedMethod("sum", WrappedTypeReference("long"), listOf(), false, MethodType.METHOD)
        val longPointer = WrappedMethod(
            "longPointer",
            WrappedTypeReference("long *"),
            listOf(),
            false,
            MethodType.METHOD,
        )
        val setSome = WrappedMethod(
            "setSome",
            WrappedTypeReference("void"),
            listOf(
                WrappedArgument("a", WrappedTypeReference("int")),
                WrappedArgument("b", WrappedTypeReference("long")),
                WrappedArgument("c", WrappedTypeReference("long long"))
            ),
            false,
            MethodType.METHOD,
        )
        val setPointers = WrappedMethod(
            "setPointers",
            WrappedTypeReference("void"),
            listOf(
                WrappedArgument("a", WrappedTypeReference("int*")),
                WrappedArgument("b", WrappedTypeReference("long*")),
                WrappedArgument("c", WrappedTypeReference("long long*"))
            ),
            false,
            MethodType.METHOD,
        )
        val setPrivateString = WrappedMethod(
            "setPrivateString",
            WrappedTypeReference("void"),
            listOf(WrappedArgument("value", WrappedTypeReference("std::string"))),
            false,
            MethodType.METHOD,
        )
        val setPrivateFrom = WrappedMethod(
            "setPrivateFrom",
            WrappedTypeReference("void"),
            listOf(WrappedArgument("value", WrappedTypeReference("TestLib::OtherClass *"))),
            false,
            MethodType.METHOD,
        )
        val output = WrappedMethod(
            "output",
            WrappedTypeReference("void"),
            listOf(),
            false,
            MethodType.METHOD,
        )
        val constructor = WrappedMethod("new", type, listOf(), false, MethodType.CONSTRUCTOR)
        val copyConstructor = WrappedMethod(
            "new",
            type,
            listOf(WrappedArgument("other", type)),
            false,
            MethodType.CONSTRUCTOR,
        )
        val otherConstructor = WrappedMethod(
            "new",
            type,
            listOf(WrappedArgument("a", WrappedTypeReference("int"))),
            false,
            MethodType.CONSTRUCTOR,
        )
        val twoParamConstructor = WrappedMethod(
            "new",
            type,
            listOf(
                WrappedArgument("a", WrappedTypeReference("int")),
                WrappedArgument("b", WrappedTypeReference("double"))
            ),
            false,
            MethodType.CONSTRUCTOR,
        )
        val destructor = WrappedMethod("~TestClass", type, listOf(), false, MethodType.DESTRUCTOR)
        val operatorMinus = WrappedMethod(
            "operator-",
            type,
            listOf(WrappedArgument("c2", type)),
            false,
            MethodType.METHOD,
        )
        val operatorUnaryMinus =
            WrappedMethod("operator-", type, listOf(), false, MethodType.METHOD)
        val operatorPlus = WrappedMethod(
            "operator+",
            type,
            listOf(WrappedArgument("c2", type)),
            false,
            MethodType.METHOD,
        )
        val operatorUnaryPlus =
            WrappedMethod("operator+", type, listOf(), false, MethodType.METHOD)
        val operatorTimes = WrappedMethod(
            "operator*",
            type,
            listOf(WrappedArgument("c2", type)),
            false,
            MethodType.METHOD,
        )
        val operatorDiv = WrappedMethod(
            "operator/",
            type,
            listOf(WrappedArgument("c2", type)),
            false,
            MethodType.METHOD,
        )
        val operatorRem = WrappedMethod(
            "operator%",
            type,
            listOf(WrappedArgument("c2", type)),
            false,
            MethodType.METHOD,
        )
        val operatorInc = WrappedMethod("operator++", type, listOf(), false, MethodType.METHOD)
        val operatorPostInc = WrappedMethod(
            "operator++",
            type,
            listOf(WrappedArgument("dummy", WrappedTypeReference("int"))),
            false,
            MethodType.METHOD,
        )
        val operatorDec = WrappedMethod("operator--", type, listOf(), false, MethodType.METHOD)
        val operatorPostDec = WrappedMethod(
            "operator--",
            type,
            listOf(WrappedArgument("dummy", WrappedTypeReference("int"))),
            false,
            MethodType.METHOD,
        )
        val operatorEq = WrappedMethod(
            "operator==",
            type,
            listOf(WrappedArgument("c2", WrappedTypeReference("TestLib::TestClass &"))),
            false,
            MethodType.METHOD,
        )
        val operatorNeq = WrappedMethod(
            "operator!=",
            type,
            listOf(WrappedArgument("c2", WrappedTypeReference("TestLib::TestClass &"))),
            false,
            MethodType.METHOD,
        )
        val operatorLt = WrappedMethod(
            "operator<",
            type,
            listOf(WrappedArgument("c2", WrappedTypeReference("TestLib::TestClass &"))),
            false,
            MethodType.METHOD,
        )
        val operatorGt = WrappedMethod(
            "operator>",
            type,
            listOf(WrappedArgument("c2", WrappedTypeReference("TestLib::TestClass &"))),
            false,
            MethodType.METHOD,
        )
        val operatorLteq = WrappedMethod(
            "operator<=",
            type,
            listOf(WrappedArgument("c2", WrappedTypeReference("TestLib::TestClass &"))),
            false,
            MethodType.METHOD,
        )
        val operatorGteq = WrappedMethod(
            "operator>=",
            type,
            listOf(WrappedArgument("c2", WrappedTypeReference("TestLib::TestClass &"))),
            false,
            MethodType.METHOD,
        )
        val operatorNot =
            WrappedMethod("operator!", referenceTo(type), listOf(), false, MethodType.METHOD)
        val operatorBinaryAnd = WrappedMethod(
            "operator&&",
            type,
            listOf(WrappedArgument("c", WrappedTypeReference("TestLib::TestClass &"))),
            false,
            MethodType.METHOD,
        )
        val operatorBinaryOr = WrappedMethod(
            "operator||",
            type,
            listOf(WrappedArgument("c2", WrappedTypeReference("TestLib::TestClass &"))),
            false,
            MethodType.METHOD,
        )
        val operatorInv =
            WrappedMethod("operator~", pointerTo(type), listOf(), false, MethodType.METHOD)
        val operatorBitwiseAnd = WrappedMethod(
            "operator&",
            type,
            listOf(WrappedArgument("c", WrappedTypeReference("TestLib::TestClass &"))),
            false,
            MethodType.METHOD,
        )
        val operatorBitwiseOr = WrappedMethod(
            "operator|",
            type,
            listOf(WrappedArgument("c2", WrappedTypeReference("TestLib::TestClass &"))),
            false,
            MethodType.METHOD,
        )
        val operatorXor = WrappedMethod(
            "operator^",
            type,
            listOf(WrappedArgument("c2", WrappedTypeReference("TestLib::TestClass &"))),
            false,
            MethodType.METHOD,
        )
        val operatorShl = WrappedMethod(
            "operator<<",
            type,
            listOf(WrappedArgument("c2", WrappedTypeReference("TestLib::TestClass &"))),
            false,
            MethodType.METHOD,
        )
        val operatorShr = WrappedMethod(
            "operator>>",
            type,
            listOf(WrappedArgument("c2", WrappedTypeReference("TestLib::TestClass &"))),
            false,
            MethodType.METHOD,
        )
        val operatorInd = WrappedMethod(
            "operator[]",
            type,
            listOf(WrappedArgument("c2", WrappedTypeReference("std::string &"))),
            false,
            MethodType.METHOD,
        )

        val cls = WrappedClass(
            "TestLib::TestClass",
            fields = listOf(

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
            ),
            methods = listOf(
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
                operatorInd,
            )
        )
    }

    object OtherClass {
        val type = WrappedTypeReference("TestLib::OtherClass")
        val constructor = WrappedMethod(
            "TestLib::OtherClass",
            type,
            emptyList(),
            false,
            MethodType.CONSTRUCTOR
        )
        val destructor = WrappedMethod(
            "~TestLib::OtherClass",
            type,
            emptyList(),
            false,
            MethodType.DESTRUCTOR
        )
        val getPrivateString = WrappedMethod(
            "getPrivateString",
            WrappedTypeReference("std::string"),
            emptyList(),
            false,
            MethodType.METHOD
        )
        val setPrivateString = WrappedMethod(
            "setPrivateString",
            WrappedTypeReference("void"),
            listOf(WrappedArgument("value", WrappedTypeReference("std::string"))),
            false,
            MethodType.METHOD
        )
        val appendText = WrappedMethod(
            "appendText",
            WrappedTypeReference("void"),
            listOf(
                WrappedArgument("text", Vector.type)
            ),
            false,
            MethodType.METHOD
        )
        val cls = WrappedClass(
            "TestLib::OtherClass",
            methods = listOf(
                constructor,
                destructor,
                getPrivateString,
                setPrivateString,
                appendText
            )
        )
    }
}
