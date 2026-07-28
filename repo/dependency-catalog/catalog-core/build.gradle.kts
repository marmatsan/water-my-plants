@file:Suppress("AvoidDuplicateDependencies")

import org.gradle.api.publish.maven.MavenPublication

plugins {
    alias(plugins.plugins.org.jetbrains.kotlin.jvm)
    alias(plugins.plugins.org.jetbrains.dokka)
    `maven-publish`
}

java {
    withSourcesJar()
}

dependencies {
    testImplementation(libs.com.marmatsan.repo.unit.test.dsl)
    testImplementation(libs.io.kotest.runner.junit5)
    testImplementation(libs.io.kotest.assertions.core)
    testRuntimeOnly(libs.org.junit.platform.launcher)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(
                components["java"],
            )
            artifactId = "catalog-core"

            pom {
                name.set("Repository Catalog Core")
                description.set("Optional portable tree DSL for dependency catalog provider implementations.")
                url.set("https://github.com/marmatsan/water-my-plants/tree/main/repo/dependency-catalog")
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
                    providers.gradleProperty("dependencyCatalogPublicationRepository").orNull
                        ?: rootProject.layout.buildDirectory
                            .dir("publication-repository")
                            .get()
                            .asFile,
                )
        }
    }
}
