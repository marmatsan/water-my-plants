@file:Suppress("AvoidDuplicateDependencies")

import org.gradle.api.publish.maven.MavenPublication

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
    alias(plugins.plugins.com.google.devtools.ksp)
    alias(plugins.plugins.org.jetbrains.dokka)
}

tasks.withType<Test> {
    systemProperty(
        "cucumber.junit-platform.naming-strategy",
        "long",
    )
    systemProperty(
        "cucumber.plugin",
        "pretty,html:build/reports/cucumber/cucumber.html,json:build/reports/cucumber/cucumber.json",
    )

    System.getProperty("cucumber.filter.tags")?.let { tags ->
        systemProperty(
            "cucumber.filter.tags",
            tags,
        )
    }
    System.getProperty("cucumber.features")?.let { features ->
        systemProperty(
            "cucumber.features",
            features,
        )
    }
}

dependencies {
    implementation(projects.domain)
    implementation(projects.data)
    implementation(libs.com.michael.bull.kotlin.result)

    ksp(libs.me.tatarka.inject.kotlin.inject.compiler.ksp)

    implementation(libs.me.tatarka.inject.kotlin.inject.runtime)
    implementation(libs.org.jetbrains.kotlinx.serialization.json)

    // Kotest
    testImplementation(libs.com.marmatsan.repo.unit.test.dsl)
    testImplementation(libs.io.kotest.runner.junit5)
    testImplementation(libs.io.kotest.assertions.core)
    // Cucumber
    testImplementation(platform(libs.io.cucumber.bom))
    testImplementation(libs.io.cucumber.java8)
    testImplementation(libs.io.cucumber.junit.platform.engine)
    testImplementation(libs.org.junit.platform.suite)
    testRuntimeOnly(libs.org.junit.platform.launcher)
}

gradlePlugin {
    val pluginName = "com.marmatsan.figmaDocumentationSync"
    plugins.register(pluginName) {
        id = pluginName
        implementationClass = "$pluginName.plugin.gradle.FigmaDocumentationSyncGradlePlugin"
        displayName = "Figma Documentation Sync"
        description = "Generates and verifies a portable Gradle repository model for Figma documentation."
    }
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        if (name == "pluginMaven") {
            artifactId = "figma-documentation-sync-gradle-plugin"
        }

        pom {
            name.set("Figma Documentation Sync Gradle Plugin")
            description.set("Gradle entry point for portable Figma design synchronization.")
            url.set("https://github.com/marmatsan/water-my-plants/tree/main/repo/figma-documentation-sync")
            scm {
                connection.set("scm:git:https://github.com/marmatsan/water-my-plants.git")
                url.set("https://github.com/marmatsan/water-my-plants")
            }
        }
    }
}
