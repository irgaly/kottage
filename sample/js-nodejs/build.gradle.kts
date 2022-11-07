import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec

plugins {
    kotlin("multiplatform")
}

kotlin {
    js(IR) {
        binaries.executable()
        nodejs()
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.kottage)
                //implementation(libs.kottage)
            }
        }
    }
}

tasks.withType<NodeJsExec>().configureEach {
    dependsOn(rootProject.tasks.named("installBetterSqlite3"))
}

