plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.serialization")
    id(Plugins.Ids.AndroidX.navigation)
}

android {
    applyCommon(project)
    defaultConfig {
        applicationId = Packages.Sample.name
        versionName = Versions.versionName
    }
}

dependencies {
    implementation(libs.androidx.appCompat)
    implementation(libs.androidx.material)
    implementation(libs.androidx.navigation.fragmentKtx)
    implementation(libs.kotlinx.serialization.json)
    implementation(project(":kotlin-kvs"))
}
