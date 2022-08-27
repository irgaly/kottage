plugins {
    id("build-logic.android.application")
    kotlin("plugin.serialization")
}

android {
    namespace = "io.github.irgaly.kottage.sample"
    defaultConfig {
        applicationId = "io.github.irgaly.kottage.sample"
        versionName = libs.versions.kottage.get()
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material)
    implementation(libs.kotlinx.serialization)
    implementation(projects.kottage)
}
