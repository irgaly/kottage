plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm()
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.kottage)
                //implementation(libs.kottage)
            }
        }
    }
}
