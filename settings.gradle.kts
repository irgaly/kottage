enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "kottage-project"
include(
    ":kottage",
    ":kottage:data:sqlite",
    ":kottage:data:indexeddb",
    ":kottage:core",
    ":kottage:core:test",
    ":sample:android",
    ":sample:multiplatform",
    ":sample:js-browser",
    ":sample:js-nodejs"
)
includeBuild("build-logic")
