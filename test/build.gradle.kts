plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()
    sourceSets {
        commonMain {
            dependencies {
                api(libs.bundles.commonTestDependencies)
            }
        }
        val jvmMain by getting {
            dependencies {
                api(libs.bundles.jvmTestDependencies)
            }
        }
    }
}
