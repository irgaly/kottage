plugins {
    id(libs.plugins.buildlogic.multiplatform.library.get().pluginId)
    id(libs.plugins.buildlogic.android.library.get().pluginId)
    alias(libs.plugins.sqldelight)
}

sqldelight {
    database("KkvsDatabase") {
        packageName = "net.irgaly.kkvs.data.sqlite"
    }
}

android {
    namespace = "net.irgaly.kkvs.data.sqlite"
}

kotlin {
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
        val androidMain by getting {
            dependencies {
                implementation(libs.sqldelight.driver.android)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(libs.sqldelight.driver.jvm)
            }
        }
        val jvmTest by getting {
            dependencies {
                api(projects.core.test)
            }
        }
        val nativeMain by getting {
            dependencies {
                implementation(libs.sqldelight.driver.native)
            }
        }
    }
}
