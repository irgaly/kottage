import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl` apply false
    kotlin("multiplatform") apply false
    id("com.android.application") apply false
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
    if (!path.startsWith(":sample") && !path.endsWith(":test")) {
        apply(plugin = "maven-publish")
        apply(plugin = "signing")
        group = "io.github.irgaly.kkvs"
        afterEvaluate {
            version = libs.versions.kkvs.get()
        }
        val emptyJavadocJar = tasks.create<Jar>("emptyJavadocJar") {
            archiveClassifier.set("javadoc")
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
            }
        }
        extensions.configure<SigningExtension> {
            useInMemoryPgpKeys(
                System.getenv("SIGNING_PGP_KEY"),
                System.getenv("SIGNING_PGP_PASSWORD")
            )
            //sign(publishing.publications)
        }
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
