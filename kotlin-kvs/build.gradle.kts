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
        val commonSqliteTest by creating {
            dependsOn(commonSqliteMain)
            dependsOn(commonTest.get())
        }
        val commonIndexeddbMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(projects.kotlinKvs.data.indexeddb)
            }
        }
        val commonIndexeddbTest by creating {
            dependsOn(commonIndexeddbMain)
            dependsOn(commonTest.get())
        }
        val androidMain by getting {
            dependsOn(commonSqliteMain)
            dependencies {
            }
        }
        val androidTest by getting {
            dependsOn(commonSqliteTest)
        }
        val jvmMain by getting {
            dependsOn(commonSqliteMain)
            dependencies {
            }
        }
        val jvmTest by getting {
            dependsOn(commonSqliteTest)
            dependencies {
            }
        }
        val jsMain by getting {
            dependsOn(commonIndexeddbMain)
            dependencies {
            }
        }
        val jsTest by getting {
            dependsOn(commonIndexeddbTest)
        }
        val nativeMain by getting {
            dependsOn(commonSqliteMain)
            dependencies {
                implementation(projects.kotlinKvs.data.sqlite)
            }
        }
        val nativeTest by getting {
            dependsOn(commonSqliteTest)
        }
    }
}

val dokkaHtml by tasks.getting(DokkaTask::class)
val javadocJar by tasks.registering(Jar::class) {
    dependsOn(dokkaHtml)
    from(dokkaHtml.outputDirectory)
    archiveClassifier.set("javadoc")
}
