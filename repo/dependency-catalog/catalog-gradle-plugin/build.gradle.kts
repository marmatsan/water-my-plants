import org.gradle.api.publish.maven.MavenPublication

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

group = "com.marmatsan.repo"
version = providers.gradleProperty("figmaDocumentationSyncVersion").getOrElse("0.1.0-SNAPSHOT")

dependencies {
    implementation(projects.catalogApi)

    testImplementation(libs.com.marmatsan.repo.unit.test.dsl)
    testImplementation(gradleTestKit())
    testImplementation(libs.io.kotest.runner.junit5)
    testImplementation(libs.io.kotest.assertions.core)
    testImplementation(libs.io.mockk)
    testRuntimeOnly(libs.org.junit.jupiter.platform.launcher)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

gradlePlugin {
    val pluginName = "com.marmatsan.dependencyCatalog"
    plugins.register(pluginName) {
        id = pluginName
        implementationClass = "com.marmatsan.dependencies.gradle.DependencyCatalogSettingsPlugin"
        displayName = "Dependency Catalog"
        description = "Registers repository-owned dependency trees as Gradle version catalogs."
    }
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        if (name == "pluginMaven") {
            artifactId = "catalog-gradle-plugin"
        }

        pom {
            name.set("Dependency Catalog Gradle Plugin")
            description.set("Gradle settings adapter for portable dependency catalog trees.")
            url.set("https://github.com/marmatsan/water-my-plants/tree/main/repo/dependency-catalog")
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
