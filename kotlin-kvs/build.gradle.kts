plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id(libs.plugins.buildlogic.android.library.get().pluginId)
    alias(libs.plugins.sqldelight)
}

sqldelight {
    database("SqlDelight") {
        packageName = "net.irgaly.kkvs"
    }
}

kotlin {
    // Android AAR
    android {
        publishAllLibraryVariants()
    }
    // Java jar
    jvm()
    // JS
    js {
        browser()
        nodejs()
    }
    // iOS
    ios()
    // ios() - iosArm64() // Apple iOS on ARM64 platforms (Apple iPhone 5s and newer)
    // ios() - iosX64() // Apple iOS simulator on x86_64 platforms
    iosSimulatorArm64() // Apple iOS simulator on Apple Silicon platforms
    // watchOS
    watchos()
    // watchos() - watchosArm64() // Apple watchOS on ARM64_32 platforms (Apple Watch Series 4 and newer)
    // watchos() - watchosX64() // Apple watchOS 64-bit simulator (watchOS 7.0 and newer) on x86_64 platforms
    watchosSimulatorArm64() // Apple watchOS simulator on Apple Silicon platforms
    // tvOS
    tvos()
    // tvos() - tvosArm64() // Apple tvOS on ARM64 platforms (Apple TV 4th generation and newer)
    // tvos() - tvosX64() // Apple tvOS simulator on x86_64 platforms
    tvosSimulatorArm64() // Apple tvOS simulator on Apple Silicon platforms
    // macOS
    macosX64() // Apple macOS on x86_64 platforms
    macosArm64() // Apple macOS on Apple Silicon platforms
    // Linux
    linuxX64() // Linux on x86_64 platforms
    // Windows
    //mingwX64() // 64-bit Microsoft Windows
    sourceSets {
        commonMain {
            dependencies {
            }
        }
        commonTest {
            dependencies {
                api(project(":test"))
            }
        }
        val jvmMain by getting {
        }
        val jsMain by getting {
        }
        val darwinMain by creating {
            // ios + macOS
            dependsOn(commonMain.get())
        }
        val linuxMain by creating {
            // Linux
            dependsOn(commonMain.get())
        }
        val linuxX64Main by getting {
            dependsOn(linuxMain)
        }
        val iosMain by getting {
            dependsOn(darwinMain)
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
        val watchosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
        val tvosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
        val macosX64Main by getting {
            dependsOn(darwinMain)
        }
        val macosArm64Main by getting {
            dependsOn(darwinMain)
        }
    }
}

