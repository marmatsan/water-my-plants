@file:Suppress("AvoidDuplicateDependencies")

import org.gradle.api.publish.maven.MavenPublication

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
    alias(plugins.plugins.org.jetbrains.dokka)
}

dependencies {
    implementation(projects.domain)
    implementation(projects.data)
    implementation(projects.teamcityAdapter)
    implementation(libs.com.michael.bull.kotlin.result)
    implementation(libs.org.jetbrains.kotlinx.serialization.json)

    testImplementation(libs.com.marmatsan.repo.unit.test.dsl)
    testImplementation(libs.bundles.kotestBundle)
    testRuntimeOnly(libs.org.junit.platform.launcher)
}

gradlePlugin {
    val pluginName = "com.marmatsan.figmaDocumentationSync.teamcityOperations"
    plugins.register(pluginName) {
        id = pluginName
        implementationClass =
            "com.marmatsan.figmaDocumentationSync.teamcity.operations.gradle." +
            "FigmaTeamCityOperationsGradlePlugin"
        displayName = "Figma Documentation Sync TeamCity Operations"
        description = "Adds optional supervised TeamCity handoff, upload, and rerun operations."
    }
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set("Figma Documentation Sync TeamCity Operations Gradle Plugin")
            description.set("Optional TeamCity operations for the portable Figma documentation sync pipeline.")
        }
    }
}
