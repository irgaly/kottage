package net.irgaly.buildlogic

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType

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
val Project.libs: VersionCatalog get() {
    return extensions.getByType<VersionCatalogsExtension>().named("libs")
}

/**
 * VersionCatalog version の取得
 */
fun VersionCatalog.version(name: String): String {
    return findVersion(name).get().requiredVersion
}
