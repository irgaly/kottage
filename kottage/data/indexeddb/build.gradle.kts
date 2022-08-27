plugins {
    kotlin("multiplatform")
    id(libs.plugins.kotest.multiplatform.get().pluginId)
}

kotlin {
    jvm()
    // JS
    js(IR) {
        browser()
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.kottage.core)
            }
        }
        commonTest {
            dependencies {
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(projects.kottage.core.test)
            }
        }
        val jsMain by getting {
            dependencies {
            }
        }
    }
}
