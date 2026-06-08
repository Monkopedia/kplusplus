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
}

rootProject.name = "kplusplus-compiler"

include(":kplusplus-compiler-gradle")
project(":kplusplus-compiler-gradle").projectDir = file("gradle")
include(":kplusplus-compiler-plugin")
project(":kplusplus-compiler-plugin").projectDir = file("plugin")
include(":kplusplus-compiler-plugin-native")
project(":kplusplus-compiler-plugin-native").projectDir = file("native")
