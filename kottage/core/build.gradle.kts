plugins {
    alias(libs.plugins.buildlogic.multiplatform.library)
    alias(libs.plugins.buildlogic.android.library)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotest)
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
