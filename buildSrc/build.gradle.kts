plugins {
    `kotlin-dsl`
}
repositories {
    google()
    gradlePluginPortal()
}
dependencies {
    implementation("com.android.tools.build:gradle:7.2.1")
    implementation("org.jetbrains.kotlin.kapt:org.jetbrains.kotlin.kapt.gradle.plugin:1.7.10")
}
