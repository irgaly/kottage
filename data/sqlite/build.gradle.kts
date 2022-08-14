plugins {
    kotlin("multiplatform")
    id(libs.plugins.buildlogic.android.library.get().pluginId)
    alias(libs.plugins.sqldelight)
}

sqldelight {
    database("KkvsDatabase") {
        packageName = "net.irgaly.kkvs.data.sqlite"
    }
}

android {
    namespace = "net.irgaly.kkvs.data.sqlite"
}

kotlin {
    // Android AAR
    android {
        publishAllLibraryVariants()
    }
    // Java jar
    jvm()
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
    mingwX64() // 64-bit Microsoft Windows
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core)
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
                api(projects.core.test)
            }
        }
        val nativeMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.sqldelight.driver.native)
            }
        }
        val darwinMain by creating {
            // ios + macOS
            dependsOn(nativeMain)
        }
        val linuxMain by creating {
            // Linux
            dependsOn(nativeMain)
        }
        val linuxX64Main by getting {
            dependsOn(linuxMain)
        }
        val iosMain by getting {
            dependsOn(darwinMain)
        }
        val watchosMain by getting {
            dependsOn(iosMain)
        }
        val tvosMain by getting {
            dependsOn(iosMain)
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
        val mingwX64Main by getting {
            dependsOn(nativeMain)
        }
    }
}
