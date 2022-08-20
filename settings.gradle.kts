enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.application") version "7.2.1"
        id("com.android.library") version "7.2.1"
        id("io.kotest.multiplatform") version "5.4.2"
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
rootProject.name = "kkvs"
include(
    ":kotlin-kvs",
    ":kotlin-kvs:data:sqlite",
    ":kotlin-kvs:data:indexeddb",
    ":kotlin-kvs:core",
    ":kotlin-kvs:core:test",
    ":sample:android"
)
includeBuild("build-logic")
