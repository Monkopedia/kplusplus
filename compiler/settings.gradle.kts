pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    // This is a separate includeBuild with its own settings, so the root
    // catalog isn't auto-imported. Wire it in explicitly so compiler-side
    // deps resolve the same versions as the rest of the build and can't drift.
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "kplusplus-compiler"

include(":kplusplus-compiler-gradle")
project(":kplusplus-compiler-gradle").projectDir = file("gradle")
include(":kplusplus-compiler-plugin")
project(":kplusplus-compiler-plugin").projectDir = file("plugin")
include(":kplusplus-compiler-plugin-native")
project(":kplusplus-compiler-plugin-native").projectDir = file("native")
