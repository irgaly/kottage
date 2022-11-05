import org.gradle.internal.os.OperatingSystem
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
            export(libs.kotlinx.coroutines.core)
            export(libs.kotlinx.serialization)
        }
    }
    ios(configure = configureXcf)
    iosSimulatorArm64(configure = configureXcf)
    watchos(configure = configureXcf)
    watchosSimulatorArm64(configure = configureXcf)
    tvos(configure = configureXcf)
    tvosSimulatorArm64(configure = configureXcf)
    macosArm64(configure = configureXcf)
    // JS
    js(IR) {
        browser {
            if (
                System.getenv().containsKey("GITHUB_ACTIONS")
                && OperatingSystem.current().isMacOsX
            ) {
                testTask {
                    useKarma {
                        useChromeHeadless()
                        useConfigDirectory(rootProject.file(".github/karma.config.d"))
                    }
                }
            }
        }
        nodejs()
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.kottage.core)
                api(libs.kotlinx.coroutines.core)
                api(libs.kotlinx.serialization)
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
        val commonIndexeddbMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(projects.kottage.data.indexeddb)
            }
        }
        val sqliteFactoryMain by creating {
            dependsOn(commonSqliteMain)
        }
        val androidMain by getting {
            dependsOn(sqliteFactoryMain)
        }
        val jvmMain by getting {
            dependsOn(sqliteFactoryMain)
        }
        val jsMain by getting {
            dependsOn(commonSqliteMain)
            dependsOn(commonIndexeddbMain)
        }
        val jsTest by getting {
            dependencies {
                // karma-safarinative-launcher だけが Safari を起動できる
                // https://github.com/karma-runner/karma-safari-launcher/issues/29#issuecomment-870387251
                implementation(devNpm("karma-safarinative-launcher", "1.1.0"))
            }
        }
        val nativeMain by getting {
            dependsOn(sqliteFactoryMain)
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
