@file:Suppress("AvoidDuplicateDependencies")

import java.net.URI
import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("org.jetbrains.kotlin.jvm")
    alias(plugins.plugins.org.jetbrains.kotlin.plugin.serialization)
    alias(plugins.plugins.org.jetbrains.dokka)
    `maven-publish`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

tasks.withType<Test> {
    useJUnitPlatform()
}

java {
    withSourcesJar()
}

dependencies {
    implementation(projects.domain)
    implementation("com.marmatsan.repo:catalog-core:${project.version}")
    testImplementation("com.marmatsan.repo:water-my-plants-catalog")

    implementation(libs.me.tatarka.inject.kotlin.inject.runtime)

    // Ktor
    implementation(platform(libs.io.ktor.bom))
    implementation(libs.io.ktor.client.core)
    implementation(libs.io.ktor.client.cio)
    implementation(libs.io.ktor.client.content.negotiation)
    implementation(libs.io.ktor.serialization.kotlinx.json)
    implementation(libs.io.modelcontextprotocol.kotlin.sdk.client)
    implementation(libs.org.jetbrains.kotlinx.serialization.json)
    implementation(libs.org.snakeyaml.engine)

    // Kotest
    testImplementation(libs.io.kotest.runner.junit5)
    testImplementation(libs.io.kotest.assertions.core)
    testRuntimeOnly(libs.org.junit.jupiter.platform.launcher)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(
                components["java"]
            )
            artifactId = "figma-documentation-sync-data"

            pom {
                name.set("Figma Documentation Sync Data")
                description.set("Portable filesystem, Gradle, catalog, and Figma adapters.")
                url.set("https://github.com/marmatsan/water-my-plants/tree/main/repo/figma-documentation-sync")
                scm {
                    connection.set("scm:git:https://github.com/marmatsan/water-my-plants.git")
                    url.set("https://github.com/marmatsan/water-my-plants")
                }
            }
        }
    }

    repositories {
        maven {
            name = "staging"
            url = uri(
                providers.gradleProperty("figmaDocumentationSyncPublicationRepository").orNull
                    ?: rootProject.layout.buildDirectory.dir("publication-repository").get().asFile
            )
        }
    }
}

dokka {
    moduleName.set("figmaDocumentationSync-data")

    dokkaPublications.html {
        includes.from(
            "docs/dokka/README.md"
        )
    }

    dokkaSourceSets.main {
        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl.set(
                URI(
                    "https://github.com/marmatsan/water-my-plants/tree/main/" +
                        "repo/figma-documentation-sync/data/src/main/kotlin"
                )
            )
            remoteLineSuffix.set("#L")
        }
    }
}
