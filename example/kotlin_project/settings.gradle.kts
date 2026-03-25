rootProject.name = "kotlin_project"
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
    includeBuild("../../")
    includeBuild("../../../klinker")
}
