import org.gradle.internal.os.OperatingSystem
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest

plugins {
    alias(libs.plugins.buildlogic.multiplatform.library)
    alias(libs.plugins.buildlogic.android.library)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.dokka)
    alias(libs.plugins.android.junit5)
}

android {
    namespace = "io.github.irgaly.kottage"
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["runnerBuilder"] =
            "de.mannodermaus.junit5.AndroidJUnit5Builder"
    }
    testOptions {
        managedDevices {
            val pixel6android13 by devices.registering(com.android.build.api.dsl.ManagedVirtualDevice::class) {
                device = "Pixel 6"
                apiLevel = 33 // Android 13
            }
            val pixel6android8 by devices.registering(com.android.build.api.dsl.ManagedVirtualDevice::class) {
                device = "Pixel 6"
                apiLevel = 27 // Android 8
            }
            groups {
                register("pixel6") {
                    targetDevices.addAll(listOf(pixel6android13.get(), pixel6android8.get()))
                }
            }
        }
    }
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
            if (providers.environmentVariable("GITHUB_ACTIONS").isPresent
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
    mingwX64 {
        binaries.configureEach {
            // rpcrt4: UuidCreate, RpcStringFreeW, UuidToStringW に必要
            linkerOpts("-lrpcrt4", "-LC:/msys64/mingw64/lib", "-lsqlite3")
        }
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
        val sqliteMain by creating {
            dependsOn(commonSqliteMain)
        }
        val androidMain by getting {
            dependsOn(sqliteMain)
        }
        val androidInstrumentedTest by getting {
            dependsOn(commonTest.get())
            dependencies {
                implementation(libs.bundles.test.android.instrumented)
            }
        }
        val jvmMain by getting {
            dependsOn(sqliteMain)
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
            dependsOn(sqliteMain)
        }
    }
    targets.withType<KotlinNativeTarget> {
        if (listOf("ios", "macos", "tvos", "watchos", "linux").any { it in name }) {
            binaries.all {
                // fix test build: "Undefined symbols for architecture arm64:"
                // https://github.com/cashapp/sqldelight/issues/3296
                // https://github.com/cashapp/sqldelight/blob/ee8eb4390dedaaf735937896aef9f0ed56f3281e/drivers/native-driver/build.gradle
                linkerOpts.add("-lsqlite3")
                if (providers.environmentVariable("GITHUB_ACTIONS").isPresent
                    && OperatingSystem.current().isLinux
                ) {
                    linkerOpts.add("-L/usr/lib/x86_64-linux-gnu")
                }
            }
        }
    }
}

dependencies {
    androidTestRuntimeOnly(libs.test.android.junit5.runner)
}

val dokkaHtml by tasks.getting(DokkaTask::class)
val javadocJar by tasks.registering(Jar::class) {
    dependsOn(dokkaHtml)
    from(dokkaHtml.outputDirectory)
    archiveClassifier.set("javadoc")
}

val jsNodeTest by tasks.named<KotlinJsTest>("jsNodeTest") {
    dependsOn(rootProject.tasks.named("installBetterSqlite3"))
}
