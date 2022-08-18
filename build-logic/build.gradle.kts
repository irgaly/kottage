plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.gradle.android)
    implementation(libs.gradle.multiplatform)
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
        register("kotlin.multiplatform") {
            id = libs.plugins.buildlogic.multiplatform.library.get().pluginId
            implementationClass = "net.irgaly.buildlogic.MultiplatformLibraryPlugin"
        }
        register("dependency-graph") {
            id = "build-logic.dependency-graph"
            implementationClass = "net.irgaly.buildlogic.ProjectDependencyGraphPlugin"
        }
    }
}
