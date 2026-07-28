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

dokka {
    moduleName.set("figmaDocumentationSync-teamcity-adapter")

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

        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl.set(
                URI(
                    "https://github.com/marmatsan/water-my-plants/tree/main/" +
                        "repo/figma-documentation-sync/teamcity-adapter/src/main/kotlin",
                ),
            )
            remoteLineSuffix.set("#L")
        }
    }
}

tasks.named("check") {
    dependsOn("dokkaGenerate")
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
    implementation(projects.domain)
    implementation(projects.data)
    implementation(libs.org.jetbrains.kotlinx.serialization.json)
    implementation(libs.org.snakeyaml.engine)

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
            artifactId = "figma-documentation-sync-teamcity-adapter"

            pom {
                name.set("Figma Documentation Sync TeamCity Adapter")
                description.set("Optional TeamCity adapter for the portable Figma design sync model.")
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
