plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()
    // JS
    js {
        browser()
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core)
            }
        }
        commonTest {
            dependencies {
            }
        }
        val jvmTest by getting {
            dependencies {
                api(projects.core.test)
            }
        }
        val jsMain by getting {
            dependencies {
            }
        }
    }
}
