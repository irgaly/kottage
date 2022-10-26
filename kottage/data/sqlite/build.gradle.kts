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
    js(IR) {
        browser()
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.kottage.core)
                implementation(libs.kotlinx.coroutines.core)
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
        val jsMain by getting {
            dependencies {
                implementation("com.squareup.sqldelight:sqljs-driver:1.5.3")
                implementation(npm("@jlongster/sql.js", "1.6.7"))
                implementation(npm("absurd-sql", "0.0.53"))
                implementation(devNpm("copy-webpack-plugin", "9.1.0"))
            }
        }
    }
}
