plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm()
    sourceSets {
        commonMain {
            dependencies {
            }
        }
        commonTest {
            dependencies {
                api(project(":test"))
            }
        }
        val jvmMain by getting {
            dependencies {
            }
        }
    }
}

