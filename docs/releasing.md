# Releasing kplusplus

kplusplus (v2) publishes to two places:

- **The Central Portal** (`central.sonatype.com`, the successor to the sunset OSSRH) — the FIR
  plugin jar, the two native tool binaries, and the Gradle plugin + its marker (so
  `mavenCentral()` consumers can resolve `id("com.monkopedia.kplusplus.compiler") version "…"`).
- **The Gradle Plugin Portal** (`plugins.gradle.org`) — the Gradle plugin, so
  `gradlePluginPortal()` consumers resolve it too.

Publishing is wired with [`com.vanniktech.maven.publish`](https://vanniktech.github.io/gradle-maven-publish-plugin/)
(POM metadata, sources/javadoc jars, GPG signing, Central Portal upload) plus
`com.gradle.plugin-publish` (the Plugin Portal upload). Everything is GPG-signed and lands in a
Central Portal **staging deployment** that the owner releases manually.

## The artifact set

| Coordinate | Where | Notes |
|---|---|---|
| `com.monkopedia.kplusplus.compiler` (plugin marker) | Plugin Portal + Central | resolves `id(...) version "..."` |
| `com.monkopedia.kplusplus:kplusplus-compiler-gradle` | Plugin Portal + Central | the Gradle plugin jar |
| `com.monkopedia.kplusplus:kplusplus-compiler-plugin` | Central | the FIR kotlinc-plugin jar |
| `com.monkopedia.kplusplus:krapper_gen` (classifier `linuxX64`) | Central | resolve+codegen tool binary |
| `com.monkopedia.kplusplus:krapper_parse` (classifier `linuxX64`) | Central | LLVM parser tool binary |

The two native tool binaries are classified, extension-less artifacts (a Kotlin/Native `.kexe`
is not a jar). Central still requires a complete POM + a sources jar + a javadoc jar + GPG
signatures per coordinate, so each ships **empty (stub) sources + javadoc jars** alongside the
binary. The `krapper_parse` stage-0 bootstrap anchor (`krapper_parse-stage0:0.2.3-stage0`) is a
mavenLocal-only artifact and is deliberately **not** published to Central.

## Required GitHub Secrets

Set these in the repository (Settings → Secrets and variables → Actions) before cutting a
release. Nothing is hardcoded; the workflow reads them all from the environment.

| Secret | What it is | Where to get it |
|---|---|---|
| `MAVEN_CENTRAL_USERNAME` | Central Portal **token username** | central.sonatype.com → Account → Generate User Token |
| `MAVEN_CENTRAL_PASSWORD` | Central Portal **token password** | (same token pair) |
| `SIGNING_KEY` | ASCII-armored GPG **private key** (the whole `-----BEGIN PGP PRIVATE KEY BLOCK-----` block) | `gpg --armor --export-secret-keys 5B83421E2338B907` |
| `SIGNING_PASSWORD` | passphrase for that GPG key | — |
| `GRADLE_PUBLISH_KEY` | Gradle Plugin Portal API key | plugins.gradle.org → your API keys |
| `GRADLE_PUBLISH_SECRET` | Gradle Plugin Portal API secret | (same) |

The legacy v1 signing key is `5B83421E2338B907` — export that same key into `SIGNING_KEY` /
`SIGNING_PASSWORD` so v2 is signed with the established identity. (v2 uses vanniktech's
**in-memory** key mechanism, not the old `signing.gnupg.keyName` in `gradle.properties`, which
was removed because its mere presence broke unsigned local `publishToMavenLocal`.)

## Cutting a release

1. Make sure `version` in the root `gradle.properties` (and the `version = "…"` in the compiler
   modules) is the release version, committed on `main`.
2. Create a GitHub **Release** (publish it). That fires `.github/workflows/release.yml`
   (`release: published`), or run the workflow manually via **workflow_dispatch**.
3. The workflow, on an LLVM-equipped runner:
   - installs the LLVM/Clang toolchain (`krapper_parse` links against `libclang-cpp`/`libLLVM`);
   - `:kplusplus-compiler-gradle:publishPlugins` → Gradle Plugin Portal;
   - `publishAllToMavenCentral -PenableClang` → uploads the signed bundle to the Central Portal
     **staging** area (`automaticRelease = false`).
4. Go to https://central.sonatype.com → **Deployments**, verify the staged deployment
   validated, and **publish** it. (Set `automaticRelease = true` in the `publishToMavenCentral`
   calls if you prefer it to auto-release.)

## Local dry run (no outward upload)

Everything is validated locally against `~/.m2` without any credentials:

```sh
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
# Full consumer set to mavenLocal (add -PenableClang to include krapper_parse):
./gradlew publishAllToMavenLocal -PenableClang
# Prove the from-published consumer still resolves + runs from mavenLocal:
(cd samples/minimal && ./gradlew runReleaseExecutableKlinker)
```

`publishToMavenLocal` does **not** sign (no key in the environment) — signing is exercised only
in CI. The only thing not verifiable without real credentials is whether the Central Portal
*accepts* the staged bundle; that is confirmed at the first real cut, in the Deployments UI.
