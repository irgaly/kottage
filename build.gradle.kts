plugins {
    `maven-publish`
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(catalog.plugins.android.classpath)
        classpath(catalog.plugins.kotlin.classpath)
        classpath(catalog.plugins.kotlin.kotlinx.serialization.classpath)
        classpath(catalog.plugins.androidx.navigation.classpath)
        classpath(catalog.plugins.remal.classpath)
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
    apply(plugin = "name.remal.check-updates")
}

subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    }
    tasks.withType<Test> {
        useJUnitPlatform {
            includeEngines("spek2")
        }
    }
}
