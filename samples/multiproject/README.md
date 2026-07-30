# samples/multiproject

The **multi-project canary**. Two Gradle projects: a root that declares the Kotlin Gradle
Plugin, and a `:bindings` subproject that applies kplusplus to one header-only C++ struct.
It is not a showcase — `samples/minimal` is the sample to read first. This one exists to keep
a specific failure mode visible.

## Why it exists

Gradle gives every project's buildscript its own classloader whose **parent** is the root
build's, and class loading is **parent-first**. A multi-project build normally declares the
Kotlin plugin once, at the root — which puts `kotlin-compiler-runner`'s own
`kotlinx-coroutines` on that *parent* classloader. A subproject applying kplusplus resolves
the coroutines version the plugin actually needs into its *child* classloader and never sees
it, because the parent answers first.

That is [#194](https://github.com/Monkopedia/kplusplus/issues/194): kplusplus 0.3.4 drove
krapper over ksrpc (#185), ktor-io called `Job.invokeOnCompletion$default` — a method
coroutines 1.9+ moved onto the interface and 1.8.0 keeps in `Job$DefaultImpls` — and every
multi-project consumer died with `NoSuchMethodError` on its first `kplusplusSync`. The fix
shades the plugin's runtime dependencies into the plugin jar (see
`compiler/gradle/build.gradle.kts`). It shipped broken because **every other sample is
single-project**, where the two plugins land in one `plugins { }` block and one classloader,
and nothing can shadow anything.

So: keep this sample two projects, and keep the Kotlin plugin declared **only at the root**.
That is the entire test. What it binds does not matter.

## Run it

The plugin is resolved by coordinate, `mavenLocal()` first. With an empty `mavenLocal` the pin
resolves the last **released** plugin, which is a useful post-release check that #194 is still
fixed in what actually shipped. To exercise the build *under test* instead, publish it locally
first — the release local dry run, from the repo root:

```sh
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
./gradlew :krapper:linkReleaseExecutableKlinker
./gradlew -p compiler publishToMavenLocal \
  -Pkpp.bundleTools.krapper="$PWD/krapper/build/bin/klinker/krapperRelease/krapper"
```

Then, from this directory:

```sh
../../gradlew :bindings:kplusplusSync
```

`kplusplusSync` is the canary: the classloader failure happens when the plugin opens the
service channel to krapper, before any binding is generated. Compiling the generated Kotlin
(`../../gradlew :bindings:build`) exercises the rest, but is not what this sample is for.

`:bindings:build` is step 4 of the release **local dry run** ([docs/releasing.md](../../docs/releasing.md)),
which is where this sample is exercised. It has no CI workflow of its own.
