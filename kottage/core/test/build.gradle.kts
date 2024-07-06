import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.buildlogic.multiplatform.library)
}

kotlin {
    js(IR) {
        browser()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.test.kotest.engine)
                implementation(libs.korlibs.time)
            }
        }
    }
}
