@file:Suppress("AvoidDuplicateDependencies")

import org.gradle.api.publish.maven.MavenPublication
import java.net.URI

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
    alias(plugins.plugins.com.google.devtools.ksp)
    alias(plugins.plugins.org.jetbrains.dokka)
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

tasks.withType<Test> {
    useJUnitPlatform()
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

java {
    withSourcesJar()
}

dependencies {
    implementation(projects.domain)
    implementation(projects.data)

    ksp(libs.me.tatarka.inject.kotlin.inject.compiler.ksp)

    implementation(libs.me.tatarka.inject.kotlin.inject.runtime)
    implementation(libs.org.jetbrains.kotlinx.serialization.json)

    // Kotest
    testImplementation(libs.io.kotest.runner.junit5)
    testImplementation(libs.io.kotest.assertions.core)
    // Cucumber
    testImplementation(platform(libs.io.cucumber.bom))
    testImplementation(libs.io.cucumber.java8)
    testImplementation(libs.io.cucumber.junit.platform.engine)
    testImplementation(libs.org.junit.platform.suite)
    testRuntimeOnly(libs.org.junit.jupiter.platform.launcher)
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

    repositories {
        maven {
            name = "staging"
            url =
                uri(
                    providers.gradleProperty("figmaDocumentationSyncPublicationRepository").orNull
                        ?: rootProject.layout.buildDirectory
                            .dir("publication-repository")
                            .get()
                            .asFile,
                )
        }
    }
}

dokka {
    moduleName.set("figmaDocumentationSync-plugin")

    dokkaPublications.html {
        includes.from(
            "docs/dokka/README.md",
        )
    }

    dokkaSourceSets.main {
        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl.set(
                URI(
                    "https://github.com/marmatsan/water-my-plants/tree/main/" +
                        "repo/figma-documentation-sync/plugin/src/main/kotlin",
                ),
            )
            remoteLineSuffix.set("#L")
        }
    }
}
