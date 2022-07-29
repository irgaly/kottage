import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl` apply false
    kotlin("multiplatform") apply false
    id("com.android.application") apply false
    id("build-logic.dependency-graph")
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

val projectDependencyGraph by tasks.getting {
    doLast {
        copy {
            from(rootProject.buildDir.resolve("reports/dependency-graph/project.dot.png"))
            into(rootProject.projectDir)
        }
    }
}
