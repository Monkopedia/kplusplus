# Releasing kplusplus

kplusplus (v2) publishes to two places:

- **The Central Portal** (`central.sonatype.com`, the successor to the sunset OSSRH) — the FIR
  plugin jar and the Gradle plugin + its marker (so `mavenCentral()` consumers can resolve
  `id("com.monkopedia.kplusplus.compiler") version "…"`). The two native tool binaries ride
  bundled inside the Gradle plugin jar, not as separate coordinates.
- **The Gradle Plugin Portal** (`plugins.gradle.org`) — the Gradle plugin, so
  `gradlePluginPortal()` consumers resolve it too.

Publishing is wired with [`com.vanniktech.maven.publish`](https://vanniktech.github.io/gradle-maven-publish-plugin/)
(POM metadata, sources/javadoc jars, GPG signing, Central Portal upload) plus
`com.gradle.plugin-publish` (the Plugin Portal upload). Everything is GPG-signed; the Central
Portal deployment **auto-releases once it passes validation** (`automaticRelease = true`), and
the Gradle plugin publishes live to the Plugin Portal — so a GitHub Release publishes everything
with no manual release step.

## The artifact set

| Coordinate | Where | Notes |
|---|---|---|
| `com.monkopedia.kplusplus.compiler` (plugin marker) | Plugin Portal + Central | resolves `id(...) version "..."` |
| `com.monkopedia.kplusplus:kplusplus-compiler-gradle` | Plugin Portal + Central | the Gradle plugin jar — **carries the bundled tool binaries** |
| `com.monkopedia.kplusplus:kplusplus-compiler-plugin` | Central | the FIR kotlinc-plugin jar |

That's the whole set — three jars, no separate native artifacts. The `krapper_gen` +
`krapper_parse` **native tool binaries are bundled INSIDE the Gradle plugin jar** (under
`/com/monkopedia/kplusplus/tools/linuxX64/`), so a consumer just applies the plugin and it
extracts + runs its own tools — no separate classified `.kexe` coordinates to resolve or chmod.
This is the 0.4.0 distribution model; it replaced the standalone
`com.monkopedia.kplusplus:krapper_gen` / `krapper_parse` classified artifacts that 0.3.0 shipped
(those remain published + immutable for 0.3.0 consumers, but are not produced going forward).

The release builds the binaries in the root build, then passes their paths to the plugin publish
via `-Pkpp.bundleTools.krapperGen=<path>` / `-Pkpp.bundleTools.krapperParse=<path>` (the compiler
build is a separate included build, so it can't depend on the tool modules directly). The plugin
jar is a normal jar — no classified-binary / empty-sources-jar machinery.

## Required GitHub Secrets

**These six secrets already exist in the repo** — they were set for the v1 pipeline and were
never removed (deleting the old `publish.yaml` workflow does not delete repo secrets). The
restored workflow **reuses the same names**, so in the common case there is nothing new to set:

| Secret (existing) | What it is | Maps to (vanniktech) | If it needs refreshing |
|---|---|---|---|
| `OSSRH_USERNAME` | Central Portal **token username** | `mavenCentralUsername` | central.sonatype.com → Account → Generate User Token (only if the old OSSRH token no longer authenticates against the Portal) |
| `OSSRH_TOKEN` | Central Portal **token password** | `mavenCentralPassword` | (same token pair) |
| `OSSRH_GPG_SECRET_KEY` | ASCII-armored GPG **private key** (`-----BEGIN PGP PRIVATE KEY BLOCK-----`) | `signingInMemoryKey` | reuse as-is (`gpg --armor --export-secret-keys 5B83421E2338B907` if ever re-exporting) |
| `OSSRH_GPG_SECRET_KEY_PASSWORD` | passphrase for that GPG key | `signingInMemoryKeyPassword` | reuse as-is |
| `GRADLE_PUBLISH_KEY` | Gradle Plugin Portal API key | — (passed via `-Pgradle.publish.key`) | reuse as-is |
| `GRADLE_PUBLISH_SECRET` | Gradle Plugin Portal API secret | — | reuse as-is |

The **only** value that might need regenerating is the Central credential (`OSSRH_USERNAME` /
`OSSRH_TOKEN`): OSSRH (oss.sonatype.org) was sunset in favour of the Central Portal, so if the
old token no longer authenticates, generate a fresh Portal user token at central.sonatype.com
and update those two values — the **names stay the same**. GPG signing and the Plugin Portal
creds are reused unchanged. The signing key is the established v1 identity `5B83421E2338B907`.
(v2 uses vanniktech's **in-memory** key mechanism, not the old `signing.gnupg.keyName` in
`gradle.properties`, which was removed because its mere presence broke unsigned local
`publishToMavenLocal`.)

## Cutting a release

1. Make sure `version` in the root `gradle.properties` (and the `version = "…"` in the compiler
   modules) is the release version, committed on `main`.
2. Create a GitHub **Release** (publish it). That fires `.github/workflows/release.yml`
   (`release: published`), or run the workflow manually via **workflow_dispatch**.
3. The workflow, on an LLVM-equipped runner:
   - installs the LLVM/Clang toolchain (`krapper_parse` links against `libclang-cpp`/`libLLVM`);
   - **builds the two tool binaries** (`:krapper_gen:linkReleaseExecutableNative` +
     `:krapper_parse:linkReleaseExecutableKlinker`) so the publish steps can bundle
     them into the plugin jar (`-Pkpp.bundleTools.<tool>=<path>`);
   - `:kplusplus-compiler-gradle:publishPlugins` → Gradle Plugin Portal (bundled);
   - `-p compiler publishToMavenCentral` → uploads the signed bundle (plugin + marker + FIR jar,
     tools bundled inside the plugin) to the Central Portal and **auto-releases it once it passes
     Portal validation** (`automaticRelease = true`). A bundle that fails validation is NOT
     released, so an invalid upload can't ship.
4. Nothing further to do on the Central side — the deployment releases itself after validation.
   Watch the workflow run (and, if you want, https://central.sonatype.com → **Deployments**) for
   status. The Gradle plugin publish to the Plugin Portal is also live immediately.
   (To require a manual release step instead, set `automaticRelease = false` in the four
   `publishToMavenCentral(...)` calls.)

## Local dry run (no outward upload)

Everything is validated locally against `~/.m2` without any credentials:

```sh
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
# 1. Build the tool binaries, then publish the plugin (tools bundled) to mavenLocal:
./gradlew :krapper_gen:linkReleaseExecutableNative :krapper_parse:linkReleaseExecutableKlinker
./gradlew -p compiler publishToMavenLocal \
  -Pkpp.bundleTools.krapperGen="$PWD/krapper_gen/build/bin/native/releaseExecutable/krapper_gen.kexe" \
  -Pkpp.bundleTools.krapperParse="$PWD/krapper_parse/build/bin/klinker/krapper_parseRelease/krapper_parse"
# 2. Prove a from-published consumer resolves the bundled tools + runs from mavenLocal:
(cd samples/minimal && ./gradlew runReleaseExecutableKlinker)
```

`publishToMavenLocal` does **not** sign (no key in the environment) — signing is exercised only
in CI. The only thing not verifiable without real credentials is whether the Central Portal
*accepts* the staged bundle; that is confirmed at the first real cut, in the Deployments UI.
