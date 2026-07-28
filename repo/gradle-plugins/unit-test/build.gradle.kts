@file:Suppress("AvoidDuplicateDependencies")

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // Modules
    implementation(projects.dependencies)
}

gradlePlugin {
    val pluginName = "com.marmatsan.unitTest"
    plugins.register(pluginName) {
        id = pluginName
        implementationClass = "$pluginName.plugin.UnitTestGradleConventionPlugin"
    }
}
