plugins {
    kotlin("multiplatform")
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
                implementation(projects.kotlinKvs.core)
            }
        }
        commonTest {
            dependencies {
            }
        }
        val jvmTest by getting {
            dependencies {
                api(projects.kotlinKvs.core.test)
            }
        }
        val jsMain by getting {
            dependencies {
            }
        }
    }
}
