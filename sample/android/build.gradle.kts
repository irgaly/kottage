plugins {
    alias(libs.plugins.buildlogic.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinx.serialization)
}

android {
    namespace = "io.github.irgaly.kottage.sample"
    defaultConfig {
        applicationId = "io.github.irgaly.kottage.sample"
        versionName = libs.versions.kottage.get()
        minSdk = 21
    }
}

dependencies {
    implementation(dependencies.platform(libs.compose.bom))
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle)
    implementation(libs.bundles.compose)
    implementation(libs.kotlinx.serialization)
    implementation(libs.faker)
    implementation(projects.kottage)
}
