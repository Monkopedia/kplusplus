rootProject.name = "kplusplus"

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}


include(":testlib_kotlin")
include(":testlib_kotlin_manual")
include(":krapper_gen")
include(":plugin")
