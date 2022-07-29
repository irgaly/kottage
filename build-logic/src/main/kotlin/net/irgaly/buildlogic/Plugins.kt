package net.irgaly.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension

// @formatter:off

val Project.catalog get(): VersionCatalog = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
val VersionCatalog.plugins get(): Plugins = Plugins(this)

class Plugins(private val catalog: VersionCatalog) {
    private fun version(name: String): String = catalog.findVersion(name).get().requiredVersion

    object Ids {
        object AndroidX {
            val navigation = "androidx.navigation.safeargs"
        }
        /**
         * Firebase に必要
         */
        val googleServices = "com.google.gms.google-services"
        val crashlytics = "com.google.firebase.crashlytics"
        val ktlint = "org.jlleitschuh.gradle.ktlint"
        val remalCheckUpdates = "name.remal.check-updates"
    }

    inner class Android {
        val classpath = "com.android.tools.build:gradle:${version("android-gradle")}"
    }
    val android = Android()

    inner class Kotlin {
        val classpath = "org.jetbrains.kotlin:kotlin-gradle-plugin:${version("kotlin-kotlin")}"
        inner class KotlinX {
            inner class Serialization {
                val classpath = "org.jetbrains.kotlin:kotlin-serialization:${version("kotlin-kotlin")}"
            }
            val serialization = Serialization()
        }
        val kotlinx = KotlinX()
    }
    val kotlin = Kotlin()

    inner class AndroidX {
        inner class Navigation {
            val classpath = "androidx.navigation:navigation-safe-args-gradle-plugin:${version("androidx-navigation")}"
        }
        val navigation = Navigation()
    }
    val androidx = AndroidX()

    inner class GoogleServices {
        val classpath = "com.google.gms:google-services:${version("google-services")}"
    }
    val googleServices = GoogleServices()

    inner class Firebase {
        inner class Crashlytics {
            val classpath = "com.google.firebase:firebase-crashlytics-gradle:${version("firebase-crashlytics-gradle")}"
        }
        val crashlytics = Crashlytics()
    }
    val firebase = Firebase()

    inner class Ktlint {
        val classpath = "org.jlleitschuh.gradle:ktlint-gradle:${version("ktlint-gradle")}"
    }
    val ktlint = Ktlint()

    /**
     * 開発便利プラグイン
     */
    inner class Remal {
        val classpath = "name.remal:gradle-plugins:${version("remal")}"
    }
    val remal = Remal()
}
