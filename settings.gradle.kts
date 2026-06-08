rootProject.name = "kplusplus"

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}

includeBuild("compiler")

include(":testlib_kotlin")
include(":testlib_kotlin_manual")
include(":krapper_gen")
include(":plugin")
include(":slice")
include(":feature-tests")
include(":featuregen")
