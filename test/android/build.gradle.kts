plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

applyCommon(android)

kotlin {
    android()
    sourceSets {
        commonMain {
            dependencies {
                api(project(":test"))
            }
        }
        val androidMain by getting {
            dependencies {
            }
        }
    }
}
