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
        kotlin("android") version "1.6.21"
        kotlin("multiplatform") version "1.6.21"
        kotlin("jvm") version "1.6.21"
        kotlin("plugin.serialization") version "1.6.21"
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
    ":sample:android",
    ":test:android",
    ":test"
)
