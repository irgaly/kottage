plugins {
    alias(libs.plugins.buildlogic.multiplatform.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotest)
}

kotlin {
    js(IR) {
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
