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
            freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
        }
    }
    tasks.withType<Test> {
        useJUnitPlatform()
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
