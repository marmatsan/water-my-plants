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

    // Build
    compileOnly(libs.com.android.tools.build.gradle)
    compileOnly(libs.org.jetbrains.kotlin.gradle.plugin)
}

gradlePlugin {
    val pluginName = "com.marmatsan.compose"
    plugins.register(pluginName) {
        id = pluginName
        implementationClass = "$pluginName.plugin.ComposeGradleConventionPlugin"
    }
}
