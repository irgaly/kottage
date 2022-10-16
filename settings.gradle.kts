enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.application") version "8.0.0-alpha05"
        id("com.android.library") version "8.0.0-alpha05"
        id("io.kotest.multiplatform") version "5.5.1"
        kotlin("android") version "1.7.10"
        kotlin("multiplatform") version "1.7.10"
        kotlin("jvm") version "1.7.10"
        kotlin("plugin.serialization") version "1.7.10"
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
    ":sample:android"
)
includeBuild("build-logic")
