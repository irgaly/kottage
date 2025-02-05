import com.android.build.gradle.BaseExtension
import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
    alias(libs.plugins.kotest.multiplatform) apply false
    alias(libs.plugins.buildlogic.multiplatform.library) apply false
    alias(libs.plugins.buildlogic.android.application) apply false
    alias(libs.plugins.buildlogic.android.library) apply false
    alias(libs.plugins.buildlogic.dependencygraph)
    alias(libs.plugins.nexus.publish)
}

subprojects {
    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging.showStandardStreams = true
    }
    listOf(
        "org.jetbrains.kotlin.android",
        "org.jetbrains.kotlin.multiplatform",
    ).forEach {
        pluginManager.withPlugin(it) {
            extensions.configure<KotlinProjectExtension> {
                jvmToolchain(17)
            }
        }
    }
    afterEvaluate {
        // libs アクセスのための afterEvaluate
        pluginManager.withPlugin(libs.plugins.kotlin.multiplatform.get().pluginId) {
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
            archiveClassifier = "javadoc"
            destinationDirectory = File(buildDir, "libs_emptyJavadoc")
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
                            name = artifactId
                            description = "Kotlin KVS Storage for Kotlin Multiplatform."
                            url = "https://github.com/irgaly/kottage"
                            developers {
                                developer {
                                    id = "irgaly"
                                    name = "irgaly"
                                    email = "irgaly@gmail.com"
                                }
                            }
                            licenses {
                                license {
                                    name = "The Apache License, Version 2.0"
                                    url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                                }
                            }
                            scm {
                                connection = "git@github.com:irgaly/kottage.git"
                                developerConnection = "git@github.com:irgaly/kottage.git"
                                url = "https://github.com/irgaly/kottage"
                            }
                        }
                    }
                }
            }
        }
        extensions.configure<SigningExtension> {
            useInMemoryPgpKeys(
                providers.environmentVariable("SIGNING_PGP_KEY").orNull,
                providers.environmentVariable("SIGNING_PGP_PASSWORD").orNull
            )
            if (providers.environmentVariable("CI").isPresent) {
                sign(extensions.getByType<PublishingExtension>().publications)
            }
        }
        tasks.withType<PublishToMavenRepository>().configureEach {
            mustRunAfter(tasks.withType<Sign>())
        }
    }
}

plugins.withType<NodeJsRootPlugin> {
    extensions.configure<NodeJsRootExtension> {
        nodeVersion = "20.18.2"
        val installBetterSqlite3 by tasks.registering(Exec::class) {
            val nodeExtension = this@configure
            val nodeEnv = nodeExtension.requireConfigured()
            val node = nodeEnv.nodeExecutable.replace(File.separator, "/")
            val nodeDir = nodeEnv.dir.path.replace(File.separator, "/")
            val nodeBinDir = nodeEnv.nodeBinDir.path.replace(File.separator, "/")
            val npmCli = if (OperatingSystem.current().isWindows) {
                "$nodeDir/node_modules/npm/bin/npm-cli.js"
            } else {
                "$nodeDir/lib/node_modules/npm/bin/npm-cli.js"
            }
            val npm = "\"$node\" \"$npmCli\""
            val betterSqlite3 = buildDir.resolve("js/node_modules/better-sqlite3")
            dependsOn(tasks.withType<KotlinNpmInstallTask>())
            inputs.files(betterSqlite3.resolve("package.json"))
            inputs.property("node-version", nodeVersion)
            outputs.files(betterSqlite3.resolve("build/Release/better_sqlite3.node"))
            outputs.cacheIf { true }
            workingDir = betterSqlite3
            commandLine = if (OperatingSystem.current().isWindows) {
                listOf(
                    "sh",
                    "-c",
                    // pwd で C:/... -> /c/... 変換
                    "PATH=\$(cd $nodeBinDir;pwd):\$PATH $npm run install --verbose"
                )
            } else {
                listOf(
                    "sh",
                    "-c",
                    "PATH=\"$nodeBinDir:\$PATH\" $npm run install --verbose"
                )
            }
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {
            // io.github.irgaly staging profile
            stagingProfileId = "6c098027ed608f"
            nexusUrl = uri("https://s01.oss.sonatype.org/service/local/")
            snapshotRepositoryUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        }
    }
}

val projectDependencyGraph by tasks.getting {
    doLast {
        copy {
            from(buildDir.resolve("reports/dependency-graph/project.md"))
            into(projectDir)
        }
    }
}
