import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    id(libs.plugins.buildlogic.multiplatform.library.get().pluginId)
    id(libs.plugins.buildlogic.android.library.get().pluginId)
    kotlin("plugin.serialization")
    alias(libs.plugins.dokka)
}

android {
    namespace = "io.github.irgaly.kottage"
}

kotlin {
    val xcf = XCFramework("Kottage")
    val configureXcf: KotlinNativeTarget.() -> Unit = {
        binaries.framework {
            baseName = "Kottage"
            xcf.add(this)
        }
    }
    ios(configure = configureXcf)
    iosSimulatorArm64(configure = configureXcf)
    watchos(configure = configureXcf)
    watchosSimulatorArm64(configure = configureXcf)
    tvos(configure = configureXcf)
    tvosSimulatorArm64(configure = configureXcf)
    macosArm64(configure = configureXcf)
    /*
    // JS
    js(IR) {
        browser()
        // nodejs has no indexeddb support
        //nodejs()
    }
     */
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.kottage.core)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization)
            }
        }
        commonTest {
            dependencies {
                implementation(projects.kottage.core.test)
                implementation(libs.klock)
            }
        }
        val commonSqliteMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(projects.kottage.data.sqlite)
            }
        }
        /*
        val commonIndexeddbMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(projects.kottage.data.indexeddb)
            }
        }
         */
        val androidMain by getting {
            dependsOn(commonSqliteMain)
            dependencies {
            }
        }
        val jvmMain by getting {
            dependsOn(commonSqliteMain)
            dependencies {
            }
        }
        /*
        val jsMain by getting {
            dependsOn(commonIndexeddbMain)
            dependencies {
            }
        }
         */
        val nativeMain by getting {
            dependsOn(commonSqliteMain)
            dependencies {
                implementation(projects.kottage.data.sqlite)
            }
        }
    }
    targets.withType<KotlinNativeTarget> {
        if (listOf("ios", "macos", "tvos", "watchos").any { it in name }) {
            binaries.all {
                // fix test build: "Undefined symbols for architecture arm64:"
                // https://github.com/cashapp/sqldelight/issues/3296
                // https://github.com/cashapp/sqldelight/blob/ee8eb4390dedaaf735937896aef9f0ed56f3281e/drivers/native-driver/build.gradle
                linkerOpts.add("-lsqlite3")
            }
        }
    }
}

val dokkaHtml by tasks.getting(DokkaTask::class)
val javadocJar by tasks.registering(Jar::class) {
    dependsOn(dokkaHtml)
    from(dokkaHtml.outputDirectory)
    archiveClassifier.set("javadoc")
}
