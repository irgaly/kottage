package io.github.irgaly.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.TaskAction

@Suppress("unused")
open class ProjectDependencyGraphTask : DefaultTask() {
    @TaskAction
    fun run() {
        val md = project.rootProject.layout.buildDirectory
            .file("reports/dependency-graph/project.md").get().asFile
        md.parentFile.mkdirs()
        md.delete()

        md.appendText(
            """
            |```mermaid
            |flowchart TD
            |    classDef mpp fill:#ffd2b3,color:#000000
            |    classDef mpp_android fill:#f7ffad,color:#000000
            |    classDef android fill:#baffc9,color:#000000
            |    classDef java fill:#ffb3ba,color:#000000
            |    classDef other fill:#eeeeee,color:#000000
            |
            """.trimMargin(),
        )

        val rootProjects = mutableListOf<Project>()
        val queue = mutableListOf(project.rootProject)
        while (queue.isNotEmpty()) {
            val project = queue.removeAt(0)
            rootProjects.add(project)
            queue.addAll(project.childProjects.values)
        }

        val projects = LinkedHashSet<Project>()
        val dependencies = LinkedHashMap<Pair<Project, Project>, MutableList<String>>()
        val multiplatformProjects = mutableListOf<Project>()
        val androidProjects = mutableListOf<Project>()
        val javaProjects = mutableListOf<Project>()
        val rankAndroid = mutableListOf<Project>()
        val rankDomain = mutableListOf<Project>()
        val rankRepository = mutableListOf<Project>()

        queue.clear()
        queue.add(project.rootProject)
        while (queue.isNotEmpty()) {
            val project = queue.removeAt(0)
            queue.addAll(project.childProjects.values)

            if (project.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
                multiplatformProjects.add(project)
            }
            if (project.plugins.hasPlugin("com.android.library")
                || project.plugins.hasPlugin("com.android.application")
            ) {
                androidProjects.add(project)
            }
            if (project.plugins.hasPlugin("java-library") || project.plugins.hasPlugin("java")) {
                javaProjects.add(project)
            }

            if (!project.path.startsWith(":core:") && project.path.endsWith(":android")) {
                rankAndroid.add(project)
            }
            if (!project.path.startsWith(":core:") && project.path.endsWith(":domain")) {
                rankDomain.add(project)
            }
            if (!project.path.startsWith(":core:") && project.path.endsWith(":repository")) {
                rankRepository.add(project)
            }

            project.configurations.all {
                getDependencies()
                    .filterIsInstance<ProjectDependency>()
                    .filter { project.path != it.path }
                    .map { project.project(it.path) }
                    .forEach { dependency ->
                        projects.add(project)
                        projects.add(dependency)
                        rootProjects.remove(dependency)

                        val graphKey = Pair(project, dependency)
                        val traits = dependencies.computeIfAbsent(graphKey) { mutableListOf() }

                        if (name.lowercase().endsWith("implementation")) {
                            traits.add("dotted")
                        }
                    }
            }
        }

        projects.sortedBy { it.path }.also {
            projects.clear()
            projects.addAll(it)
        }

        md.appendText("\n    %% Modules\n\n")
        for (project in projects) {
            var brackets = Pair("[", "]")
            if (rootProjects.contains(project)) {
                brackets = Pair("([", "])")
            }

            var styleClass = "other"
            if (multiplatformProjects.contains(project)) {
                if (androidProjects.contains(project)) {
                    styleClass = "mpp_android"
                } else {
                    styleClass = "mpp"
                }
            } else if (androidProjects.contains(project)) {
                styleClass = "android"
            } else if (javaProjects.contains(project)) {
                styleClass = "java"
            }

            md.appendText(
                """
                |    ${project.path}${brackets.first}${project.path}${brackets.second}; class ${project.path} $styleClass
                |
            """.trimMargin(),
            )
        }

        md.appendText("\n    %% Dependencies\n\n")
        dependencies.forEach { (key, traits) ->
            var link = "-->"
            if (traits.contains("dotted")) {
                link = "-.->"
            }
            md.appendText("    ${key.first.path} $link ${key.second.path}\n")
        }

        md.appendText("```\n")
        println("Project module dependency graph created at ${md.absolutePath}")
    }
}

