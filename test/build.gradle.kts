plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()
    sourceSets {
        commonMain {
            dependencies {
            }
        }
        val jvmMain by getting {
            dependencies {
            }
        }
    }
}
