import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.buildlogic.multiplatform.library)
    alias(libs.plugins.buildlogic.android.library)
    alias(libs.plugins.kotlinx.serialization)
}

android {
    namespace = "io.github.irgaly.kottage.core"
}

kotlin {
    // JS
    js(IR) {
        browser()
        nodejs()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        commonTest {
            dependencies {
                implementation(projects.kottage.core.test)
            }
        }
    }
}
