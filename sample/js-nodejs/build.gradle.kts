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
            }
        }
        val jsMain by getting {
            dependencies {
            }
        }
    }
}

tasks.withType<NodeJsExec>().configureEach {
    dependsOn(rootProject.tasks.named("installBetterSqlite3"))
}

