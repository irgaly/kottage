plugins {
    alias(libs.plugins.buildlogic.multiplatform.library)
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
