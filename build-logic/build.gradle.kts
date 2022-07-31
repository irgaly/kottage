plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.gradle.android)
}

gradlePlugin {
    plugins {
        register("android.application") {
            id = "build-logic.android.application"
            implementationClass = "net.irgaly.buildlogic.AndroidApplicationPlugin"
        }
        register("android.library") {
            id = libs.plugins.buildlogic.android.library.get().pluginId
            implementationClass = "net.irgaly.buildlogic.AndroidLibraryPlugin"
        }
        register("dependency-graph") {
            id = "build-logic.dependency-graph"
            implementationClass = "net.irgaly.buildlogic.ProjectDependencyGraphPlugin"
        }
    }
}
