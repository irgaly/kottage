plugins {
    kotlin("multiplatform")
}

kotlin {
    js(IR) {
        binaries.executable()
        nodejs()
    }
    sourceSets {
        commonMain {
            dependencies {
            }
        }
        val jsMain by getting {
            dependencies {
            }
        }
    }
}
