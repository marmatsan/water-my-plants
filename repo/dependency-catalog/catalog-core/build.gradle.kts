import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("org.jetbrains.kotlin.jvm")
    `maven-publish`
}

group = "com.marmatsan.repo"
version = providers.gradleProperty("figmaDesignSyncVersion").getOrElse("0.1.0-SNAPSHOT")

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "catalog-core"

            pom {
                name.set("Repository Catalog Core")
                description.set("Portable dependency catalog model used by Figma Design Sync adapters.")
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
            url = uri(
                providers.gradleProperty("figmaDesignSyncPublicationRepository").orNull
                    ?: rootProject.layout.buildDirectory.dir("publication-repository").get().asFile
            )
        }
    }
}
