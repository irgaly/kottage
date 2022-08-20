import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    id(libs.plugins.buildlogic.multiplatform.library.get().pluginId)
    id(libs.plugins.buildlogic.android.library.get().pluginId)
    kotlin("plugin.serialization")
    alias(libs.plugins.dokka)
}

android {
    namespace = "io.github.irgaly.kkvs"
}

kotlin {
    // JS
    js(IR) {
        browser()
        // nodejs has no indexeddb support
        //nodejs()
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.kotlinKvs.core)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization)
            }
        }
        commonTest {
            dependencies {
                implementation(projects.kotlinKvs.core.test)
            }
        }
        val commonSqliteMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(projects.kotlinKvs.data.sqlite)
            }
        }
        val commonIndexeddbMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(projects.kotlinKvs.data.indexeddb)
            }
        }
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
        val jsMain by getting {
            dependsOn(commonIndexeddbMain)
            dependencies {
            }
        }
        val nativeMain by getting {
            dependsOn(commonSqliteMain)
            dependencies {
                implementation(projects.kotlinKvs.data.sqlite)
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
