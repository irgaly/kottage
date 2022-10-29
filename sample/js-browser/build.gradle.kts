plugins {
    kotlin("multiplatform")
}

kotlin {
    js(IR) {
        binaries.executable()
        browser()
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.kottage)
            }
        }
        val jsMain by getting {
            dependencies {
            }
        }
    }
}
