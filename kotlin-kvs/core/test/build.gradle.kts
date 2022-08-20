plugins {
    id(libs.plugins.buildlogic.multiplatform.library.get().pluginId)
}

kotlin {
    js(IR) {
        browser()
    }
    sourceSets {
        commonMain {
            dependencies {
            }
        }
    }
}
