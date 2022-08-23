plugins {
    id(libs.plugins.buildlogic.multiplatform.library.get().pluginId)
    id(libs.plugins.buildlogic.android.library.get().pluginId)
    kotlin("plugin.serialization")
}

android {
    namespace = "io.github.irgaly.kkvs.core"
}

kotlin {
    // JS
    js(IR) {
        browser()
        // nodejs has no indexeddb support
        //nodejs()
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        commonTest {
            dependencies {
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(projects.kotlinKvs.core.test)
            }
        }
    }
}
