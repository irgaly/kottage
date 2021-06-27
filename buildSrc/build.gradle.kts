plugins {
    `kotlin-dsl`
}
repositories {
    google()
    gradlePluginPortal()
}
dependencies {
    implementation("com.android.tools.build:gradle:7.1.0-alpha02")
    implementation("org.jetbrains.kotlin.kapt:org.jetbrains.kotlin.kapt.gradle.plugin:1.5.20")
}
