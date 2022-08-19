plugins {
    id(libs.plugins.buildlogic.multiplatform.library.get().pluginId)
    id(libs.plugins.buildlogic.android.library.get().pluginId)
    alias(libs.plugins.sqldelight)
}

sqldelight {
    database("KkvsDatabase") {
        packageName = "io.github.irgaly.kkvs.data.sqlite"
    }
}

android {
    namespace = "io.github.irgaly.kkvs.data.sqlite"
}

kotlin {
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
                api(projects.kotlinKvs.core.test)
            }
        }
        val nativeMain by getting {
            dependencies {
                implementation(libs.sqldelight.driver.native)
            }
        }
    }
}
