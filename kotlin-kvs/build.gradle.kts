plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id(libs.plugins.buildlogic.android.library.get().pluginId)
    alias(libs.plugins.sqldelight)
    id("maven-publish")
    id("signing")
    alias(libs.plugins.dokka)
}

sqldelight {
    database("Database") {
        packageName = "net.irgaly.kkvs"
    }
}

android {
    namespace = "net.irgaly.kkvs"
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
    mingwX64() // 64-bit Microsoft Windows
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.serialization)
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
                api(projects.test)
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(libs.sqldelight.driver.js)
                implementation(libs.kotlinx.coroutines.js)
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

val dokkaHtml by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)
val javadocJar by tasks.registering(Jar::class) {
    dependsOn(dokkaHtml)
    from(dokkaHtml.outputDirectory)
    archiveClassifier.set("javadoc")
}

signing {
    useInMemoryPgpKeys(System.getenv("SIGNING_PGP_KEY"), System.getenv("SIGNING_PGP_PASSWORD"))
    //sign(publishing.publications)
}

group = "io.github.irgaly.kkvs"
version = libs.versions.kkvs.get()

publishing {
    publications.withType<MavenPublication> {
        artifact(javadocJar)
        pom {
            name.set(artifactId)
            description.set("")
            url.set("https://github.com/irgaly/kotlin-kvs")
            developers {
                developer {
                    id.set("irgaly")
                    name.set("irgaly")
                    email.set("irgaly@gmail.com")
                }
            }
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            scm {
                connection.set("git@github.com:irgaly/kotlin-kvs.git")
                developerConnection.set("git@github.com:irgaly/kotlin-kvs.git")
                url.set("https://github.com/irgaly/kotlin-kvs")
            }
        }
    }
}
