import io.github.irgaly.buildlogic.execute
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl` apply false
    kotlin("multiplatform") apply false
    id("com.android.application") apply false
    id(libs.plugins.kotest.multiplatform.get().pluginId) apply false
    id("build-logic.dependency-graph")
    alias(libs.plugins.nexus.publish)
}

subprojects {
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
        }
    }
    tasks.withType<Test> {
        useJUnitPlatform()
    }
    pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets {
                val commonTest by getting {
                    dependencies {
                        implementation(libs.bundles.test.common)
                    }
                }
            }
            afterEvaluate {
                sourceSets {
                    findByName("jvmTest")?.apply {
                        dependencies {
                            implementation(libs.test.kotest.runner)
                        }
                    }
                }
            }
        }
    }

    if (!path.startsWith(":sample") && !path.endsWith(":test")) {
        apply(plugin = "maven-publish")
        apply(plugin = "signing")
        group = "io.github.irgaly.kottage"
        afterEvaluate {
            // afterEvaluate for accessing version catalogs
            version = libs.versions.kottage.get()
        }
        val emptyJavadocJar = tasks.create<Jar>("emptyJavadocJar") {
            archiveClassifier.set("javadoc")
            destinationDirectory.set(File(buildDir, "libs_emptyJavadoc"))
        }
        extensions.configure<PublishingExtension> {
            afterEvaluate {
                afterEvaluate {
                    // KotlinMultiplatformPlugin は afterEvaluate により Android Publication を生成する
                    // 2 回目の afterEvaluate 以降で Android Publication にアクセスできる
                    publications.withType<MavenPublication>().all {
                        var javadocJar: Task? = emptyJavadocJar
                        var artifactSuffix = "-$name"
                        if (name == "kotlinMultiplatform") {
                            artifactSuffix = ""
                            javadocJar = tasks.findByName("javadocJar") ?: emptyJavadocJar
                        }
                        artifact(javadocJar)
                        artifactId = "${path.split(":").drop(1).joinToString("-")}$artifactSuffix"
                        pom {
                            name.set(artifactId)
                            description.set("Kotlin KVS Storage for Kotlin Multiplatform.")
                            url.set("https://github.com/irgaly/kottage")
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
                                connection.set("git@github.com:irgaly/kottage.git")
                                developerConnection.set("git@github.com:irgaly/kottage.git")
                                url.set("https://github.com/irgaly/kottage")
                            }
                        }
                    }
                }
            }
        }
        extensions.configure<SigningExtension> {
            useInMemoryPgpKeys(
                System.getenv("SIGNING_PGP_KEY"),
                System.getenv("SIGNING_PGP_PASSWORD")
            )
            sign(extensions.getByType<PublishingExtension>().publications)
        }
    }
}

plugins.withType<NodeJsRootPlugin> {
    configure<NodeJsRootExtension> {
        // kotlinNpmInstall で使う node をなるべく実行環境のものに揃える
        nodeVersion = execute("node --version || echo v18.12.0").removePrefix("v")
    }
}

val installBetterSqlite3 by tasks.registering(Exec::class) {
    val betterSqlite3 = rootProject.buildDir.resolve("js/node_modules/better-sqlite3")
    mustRunAfter(rootProject.tasks.withType<org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask>())
    inputs.files(betterSqlite3.resolve("package.json"))
    outputs.files(betterSqlite3.resolve("build/Release/better_sqlite3.node"))
    outputs.cacheIf { true }
    workingDir = betterSqlite3
    commandLine = if (System.getenv().containsKey("GITHUB_ACTIONS")) {
        listOf("npm", "run", "install")
    } else {
        // pyenv で python2 をインストールしている前提で実行する
        listOf("sh", "-c", "PATH=$(pyenv root)/shims:\$PATH npm run install")
    }
}

nexusPublishing {
    repositories {
        sonatype {
            // io.github.irgaly staging profile
            stagingProfileId.set("6c098027ed608f")
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}

val projectDependencyGraph by tasks.getting {
    doLast {
        copy {
            from(rootProject.buildDir.resolve("reports/dependency-graph/project.dot.png"))
            into(rootProject.projectDir)
        }
    }
}
