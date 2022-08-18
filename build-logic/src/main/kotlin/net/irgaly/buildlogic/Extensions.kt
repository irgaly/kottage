package net.irgaly.buildlogic

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * android build 共通設定を適用する
 */
fun Project.configureAndroid() {
    extensions.configure<BaseExtension> {
        compileSdkVersion(libs.version("gradle-android-compile-sdk").toInt())
        defaultConfig {
            minSdk = libs.version("gradle-android-min-sdk").toInt()
            targetSdk = libs.version("gradle-android-target-sdk").toInt()
        }
    }
}

/**
 * android library 共通設定を適用する
 */
fun Project.configureAndroidLibrary() {
    extensions.configure<LibraryExtension> {
        buildFeatures {
            buildConfig = false
        }
        sourceSets.configureEach {
            setRoot("src/android/$name")
            java.srcDirs("src/android/$name/kotlin")
        }
    }
}

/**
 * VersionCatalog の取得
 */
val Project.libs: VersionCatalog
    get() {
        return extensions.getByType<VersionCatalogsExtension>().named("libs")
    }

/**
 * VersionCatalog version の取得
 */
fun VersionCatalog.version(name: String): String {
    return findVersion(name).get().requiredVersion
}

/**
 * multiplatform library 共通設定
 */
fun Project.configureMultiplatformLibrary() {
    extensions.configure<KotlinMultiplatformExtension> {
        pluginManager.withPlugin("com.android.library") {
            // Android AAR
            android {
                publishAllLibraryVariants()
            }
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
            val commonMain by getting
            val nativeMain by creating {
                dependsOn(commonMain)
            }
            val darwinMain by creating {
                dependsOn(nativeMain)
            }
            val linuxMain by creating {
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
}
