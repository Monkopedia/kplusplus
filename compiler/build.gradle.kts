allprojects {
    group = "com.monkopedia.kplusplus"
    version = "0.3.0"
}

// R1 (#128): aggregate publish for the two consumer-facing JVM artifacts that live in this
// (included) `compiler` build — the Gradle plugin (+ its plugin marker) and the FIR kotlinc
// plugin jar. The root build's `publishAllToMavenLocal` invokes this via the included build
// so a single command publishes the whole consumer set to mavenLocal. mavenLocal only.
tasks.register("publishCompilerToMavenLocal") {
    group = "kplusplus"
    description =
        "Publish the Gradle plugin (+ marker) and the FIR kotlinc-plugin jar to mavenLocal."
    dependsOn(
        ":kplusplus-compiler-gradle:publishToMavenLocal",
        ":kplusplus-compiler-plugin:publishToMavenLocal"
    )
}
