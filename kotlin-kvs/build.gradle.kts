plugins {
    id(libs.plugins.buildlogic.multiplatform.library.get().pluginId)
    id(libs.plugins.buildlogic.android.library.get().pluginId)
    kotlin("plugin.serialization")
    id("maven-publish")
    id("signing")
    alias(libs.plugins.dokka)
}

android {
    namespace = "net.irgaly.kkvs"
}

kotlin {
    // JS
    js {
        browser()
        // nodejs has no indexeddb support
        //nodejs()
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization)
            }
        }
        commonTest {
            dependencies {
            }
        }
        val commonSqliteMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(projects.data.sqlite)
            }
        }
        val commonIndexeddbMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(projects.data.indexeddb)
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
        val jvmTest by getting {
            dependencies {
                api(projects.core.test)
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
                implementation(projects.data.sqlite)
            }
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
