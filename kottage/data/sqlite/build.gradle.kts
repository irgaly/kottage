plugins {
    id(libs.plugins.buildlogic.multiplatform.library.get().pluginId)
    id(libs.plugins.buildlogic.android.library.get().pluginId)
    alias(libs.plugins.sqldelight)
}

sqldelight {
    database("KottageDatabase") {
        packageName = "io.github.irgaly.kottage.data.sqlite"
    }
}

android {
    namespace = "io.github.irgaly.kottage.data.sqlite"
}

kotlin {
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
                implementation(projects.kottage.core.test)
            }
        }
        val nativeMain by getting {
            dependencies {
                implementation(libs.sqldelight.driver.native)
            }
        }
    }
}