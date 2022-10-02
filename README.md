# K++

Tools to aid in calling c++ from kotlin/native.

## Backstory

This is a bit of pointless story about K++ becoming what it is, skip it if you find it boring.

This all started because I wanted some kotlin/native code I was writing to call into v8. I thought
no problem, kotlin/native has C interop, so it shouldn't be a big deal. However it turns out that
v8 does not have a C API, and kotlin/native does not interop with C++, only C and Objective-C.

I did countless hours of research thinking this must be a mistake. Someone out there must be
using C++ with Kotlin/Native, I could have sworn I had even heard of it. However, I found nothing
generalizable, just instructions on how to write a couple of wrapping methods to call from C.
I thought this must be an oversight, it can't be that hard, I'll just go do it myself.

Well I created this repository over a year before writing this README, and now finally have
something which, might work, in just a few cases, sometimes, and that was not easy.

At first, I thought that I simply needed to create typeless wrappers that manually casted to the
correct things, and it would magically work. The hardest thing about that would be tying into
MemScoped and make sure that allocations/frees happened in the right places. Then I realized I
had to parse out the type information so I could handle constants, pointers, references, etc.
After this I realized all of the special handling required for native types and strings.

After several layers of finding new complexities to make the project even harder, I finally ran
into templates. At the beginning I thought templates were something I could shelve until later
on, however I hadn't accounted for how much STL is used throughout C++ code (I have never really
used C++ in a professional setting, and have a lot of gaps in knowledge with it). I also had not
considered the fact that C++ templates and kotlin templates were so fundamentally different,
because C++ generates the specializations of templates at compile time.

My early attempts at templates were as silly as hardcoding pieces of STL into my wrapper program,
and then to generate them for any referenced types. This quickly became clear that this was not
nearly sufficient, hard coding still couldn't handle the complexity required when a template class
has a method that returns a template type. That method would return something that could be native,
or a pointer, or a const, or some other combination of thengs that affect how the code should
be wrapped.

At this point I realized why no one more familiar with C++ had done this, and I thought
my attempts were dead. There was simply no way to create a problem that could handle wrapping more
than one C++ library because of the innate complexity and special cases needed to support even one
moderate size library.

The revival came while realizing not to solve it for all libraries, instead solve it part of the
way for all, and use well-tooled manual intervention for the last mile of special cases. This
involved switching to a 3-stage system, parse, resolve, and generate, which allows for easy
customization at the resolve stage. This customization is indended to be as light weight as
possible and easy to do from the build system.

# Tools

Currenttly K++ contains 2 tools, and also is frequently used with
[klinker](https://github.com/Monkopedia/klinker) for builds. The parsing and wrapping of code is
done using krapper, and integration of krapper into gradle builds is done with the K++ gradle
plugin.

## Krapper

Krapper gen (pronounced wrapper) is a standalone executable that creates the wrapping code for
C++ and kotlin. It handles it in 3 stages, it parses the header into methods and types, then
resolves references contained within those types, then generates code which wraps all of classes
and methods.

The executable can be controlled as much as possible from the command line, however the design is
to be used in a service mode, using `-s` where it hosts a ksrpc service an stdin/out with the
interface defined in [KrapperService](krapper_gen/src/commonMain/kotlin/KrapperService.kt).

### Parsing

Parsing of the code is done using libclang's C indexing API. Then it traverses the tree produced
and the classes included into its own wrapping implementation (Wrapped\* classes).

### Resolving

Resolving ensures references from the wrapped instances exist before turning them into Resolved\*
classes, and allows custom mapping through the ksrpc service. Custom mappings can add, remove, or
modify the structure of the Resolved\* tree/instances, which will modify what code is generated in
the third step.

### Output

During output, krapper generates C/C++ headers that wrap the relevant C++ classes, then it compiles
the code into a static library and generates the kotlin code to call into the C-interop compatible
headers.

## K++ Gradle Plugin

The K++ gradle plugin is mostly designed at making it easy to embed Krapper in a gradle build.
However it does do a bit of work to support a clean API for doing the custom mappings needed by
krapper from a gradle build script, this allows for a very integrated interop experience. For
example, the following clears the const return type off a number of methods to correct the
generated code.

```
kplusplus {
    config {
        referencePolicy = ReferencePolicy.INCLUDE_MISSING
    }
    import {
        ...
        map(ResolvedMethod) {
            find {
                (methodReturnType startsWith "const v8::Local<") or
                    (methodReturnType startsWith "const v8::Maybe<") or
                    (methodReturnType startsWith "const v8::MaybeLocal<") or
                    (methodReturnType startsWith "const v8::ScriptOrigin<") or
                    (methodReturnType startsWith "const v8::Location<")
            }
            onEach { element ->
                println("Clearing const return type on $element")
                val nonConstReturn = element.returnType.copy(
                    typeString = element.returnType.typeString.removePrefix("const ")
                )
                element.replaceWith(element.copy(returnType = nonConstReturn))
            }
        }
    }
}
```

The plugin generates an instance of the import for each compilation on the project and connects
a dependent task to execute krapper as needed. It also generates the necessary cinterop declaration
that uses the output from krapper.

# Usage

The simplest usage of K++ is to include the gradle plugin and then configure it from a gradle file
directly.

```
plugins {
    ...
    id("com.monkopedia.kplusplus.plugin")
}
```

Then from kplusplus, the common config can be declared, this includes things like how to handle
errors and where the compiler lives, etc.

```
kplusplus {
    config {
        compiler = "./g++" // Compiler path is picked up from konan if unspecified.
        pkg = "com.monkopedia.example.c++"
        moduleName = "kplusplus_example"
        errorPolicy = ErrorPolicy.LOG
        referencePolicy = ReferencePolicy.INCLUDE_MISSING
        debug = true // Sets extra debugging inside krapper gen executable
    }
    ...
}
```

Then a number of imports may be defined, although its likely its just one. Imports are primarily
the definition of the headers and library being imported, followed by a series of mappings that get
executed during wrapping.

```
kplusplus {
    ...
    import {
        // library and headers sourcesets specify the target files

        // Mappings are filtered with a DSL for selection to speed up the process
        // then executed with arbitrary code as a callback into the gradle process.
        // All resolved types are immutable, so changes can only be made by calling add, remove, or
        // replaceWith from within a mapping function.
        // This simply targets a number of methods that didn't wrap properly and are preventing
        // compiling
        map(ResolvedMethod) {
            find {
                parent(qualified eq "v8::Persistent<v8::Value>") or
                    parent(qualified eq "v8::platform::tracing::TraceWriter")
            }
            onEach { element ->
                if (element.uniqueCName == "_v8_Persistent_v8_Value_new" ||
                    element.uniqueCName == "v8_Persistent_v8_Value_op_assign" ||
                    element.uniqueCName == "v8_platform_tracing_TraceWriter_create_system_instrumentation_trace_writer"
                ) {
                    println("Removing $element")
                    element.remove()
                }
            }
        }
    }
}
```

For a complete example of usage, see the [example](example) which wraps and calls into v8 code.

# Limitations / Problems

## User intervention

The biggest limitation is the problem that usage/wrappers of any given library generally require
some hand crafting of the wrappers for it. It would be nice to remove this, but doesn't seem
feasible for me at the moment.

## Template specialization

Currently template specialization wrappers are generated for any reference to a specialization of
a template detected during resolving. In the future this should be expanded to have an easy way to
add new ones at the mapping step, but for now this is quite a limatiton as extra references to
dummy specializations would be needed for those wrappers to be generated.

## Inheritance

Currently base classes are handled by adding wrappers of the base classes to the wrapper of the
class itself. This works for giving access to the basic methods, but does not handle casting at all
and causes problems in calling methods.

## Abstract Classes/Interfaces

Currently there is no support for extending classes, let alone the case where methods need to be
implemented.
