package io.github.irgaly.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

class ProjectDependencyGraphPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        target.tasks.register<ProjectDependencyGraphTask>("projectDependencyGraph")
    }
}

