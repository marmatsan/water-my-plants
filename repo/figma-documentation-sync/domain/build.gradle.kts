@file:Suppress("AvoidDuplicateDependencies")

import org.gradle.api.publish.maven.MavenPublication
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import java.net.URI

plugins {
    kotlin("jvm")
    alias(plugins.plugins.org.jetbrains.dokka)
    `java-library`
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
    api(libs.com.michael.bull.kotlin.result)
    implementation(libs.me.tatarka.inject.kotlin.inject.runtime)

    // Kotest
    testImplementation(libs.com.marmatsan.repo.unit.test.dsl)
    testImplementation(libs.io.kotest.runner.junit5)
    testImplementation(libs.io.kotest.assertions.core)
    testRuntimeOnly(libs.org.junit.jupiter.platform.launcher)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(
                components["java"],
            )
            artifactId = "figma-documentation-sync-domain"

            pom {
                name.set("Figma Documentation Sync Domain")
                description.set("Portable models and ports for Figma design synchronization.")
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
    moduleName.set("figmaDocumentationSync-domain")

    dokkaPublications.html {
        failOnWarning.set(true)
        includes.from(
            "docs/dokka/README.md",
        )
    }

    dokkaSourceSets.main {
        documentedVisibilities.set(
            setOf(
                VisibilityModifier.Public,
                VisibilityModifier.Internal,
            ),
        )
        reportUndocumented.set(true)

        samples.from(
            file("src/main/kotlin/com/marmatsan/figmaDocumentationSync/domain/samples/DomainKDocSamples.kt"),
        )

        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl.set(
                URI(
                    "https://github.com/marmatsan/water-my-plants/tree/main/" +
                        "repo/figma-documentation-sync/domain/src/main/kotlin",
                ),
            )
            remoteLineSuffix.set("#L")
        }
    }
}

tasks.named("check") {
    dependsOn("dokkaGenerate")
}
