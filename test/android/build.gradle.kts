plugins {
    kotlin("multiplatform")
    id("build-logic.android.library")
}

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
