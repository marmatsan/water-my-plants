@file:Suppress("AvoidDuplicateDependencies")

import java.net.URI
import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("org.jetbrains.kotlin.jvm")
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
    implementation(libs.me.tatarka.inject.kotlin.inject.runtime)

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
            url = uri(
                providers.gradleProperty("figmaDocumentationSyncPublicationRepository").orNull
                    ?: rootProject.layout.buildDirectory.dir("publication-repository").get().asFile
            )
        }
    }
}

dokka {
    moduleName.set("figmaDocumentationSync-domain")

    dokkaPublications.html {
        includes.from(
            "docs/dokka/README.md"
        )
    }

    dokkaSourceSets.main {
        samples.from(
            file("src/main/kotlin/com/marmatsan/figmaDocumentationSync/domain/samples/DomainKDocSamples.kt")
        )

        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl.set(
                URI(
                    "https://github.com/marmatsan/water-my-plants/tree/main/" +
                        "repo/figma-documentation-sync/domain/src/main/kotlin"
                )
            )
            remoteLineSuffix.set("#L")
        }
    }
}
