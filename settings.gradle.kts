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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version ("0.9.0")
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
