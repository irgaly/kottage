pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}
rootProject.name = "kotlin-kvs"
enableFeaturePreview("VERSION_CATALOGS")
include(
    ":kotlin-kvs",
    ":sample:android",
    ":test:android",
    ":test"
)
