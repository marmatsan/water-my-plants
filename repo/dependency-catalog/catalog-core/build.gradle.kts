import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("org.jetbrains.kotlin.jvm")
    `maven-publish`
}

group = "com.marmatsan.repo"
version = providers.gradleProperty("figmaDocumentationSyncVersion").getOrElse("0.1.0-SNAPSHOT")

java {
    withSourcesJar()
}

dependencies {
    testImplementation(libs.io.kotest.runner.junit5)
    testImplementation(libs.io.kotest.assertions.core)
    testRuntimeOnly(libs.org.junit.jupiter.platform.launcher)
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
                    providers.gradleProperty("figmaDocumentationSyncPublicationRepository").orNull
                        ?: rootProject.layout.buildDirectory
                            .dir("publication-repository")
                            .get()
                            .asFile,
                )
        }
    }
}
