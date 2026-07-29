@file:Suppress("AvoidDuplicateDependencies")

import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import java.net.URI
import java.util.Properties

plugins {
    base
    alias(plugins.plugins.org.jetbrains.dokka) apply false
    alias(plugins.plugins.org.jetbrains.kotlin.jvm) apply false
}

val versions: Properties =
    Properties().apply {
        file("versions.properties").inputStream().use(::load)
    }
val publicationVersion: String =
    providers.gradleProperty("dependencyCatalogVersion").getOrElse(
        versions.getProperty("dependencyCatalogVersion"),
    )
val stagingPublicationRepository: String =
    providers.gradleProperty("dependencyCatalogPublicationRepository").orNull
        ?: layout.buildDirectory
            .dir("publication-repository")
            .get()
            .asFile.absolutePath

allprojects {
    group = "com.marmatsan.repo"
    version = publicationVersion
}

subprojects {
    pluginManager.withPlugin("java") {
        extensions.configure<JavaPluginExtension> {
            withSourcesJar()
        }
        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }
    }

    pluginManager.withPlugin("org.jetbrains.dokka") {
        extensions.configure<DokkaExtension> {
            moduleName.convention(
                path.removePrefix(":").replace(
                    ':',
                    '/',
                ),
            )

            dokkaPublications.configureEach {
                failOnWarning.set(true)
            }

            dokkaSourceSets.configureEach {
                documentedVisibilities.set(
                    setOf(
                        VisibilityModifier.Public,
                        VisibilityModifier.Internal,
                    ),
                )
                reportUndocumented.set(true)
                skipEmptyPackages.set(true)
                suppressGeneratedFiles.set(true)

                val localSourceDirectory = layout.projectDirectory.dir("src/main/kotlin")
                if (localSourceDirectory.asFile.exists()) {
                    sourceLink {
                        localDirectory.set(localSourceDirectory)
                        remoteUrl.set(
                            URI(
                                "https://github.com/marmatsan/water-my-plants/tree/main/" +
                                    "repo/dependency-catalog/" +
                                    localSourceDirectory.asFile
                                        .relativeTo(rootProject.projectDir)
                                        .invariantSeparatorsPath,
                            ),
                        )
                        remoteLineSuffix.set("#L")
                    }
                }
            }
        }

        tasks.matching { task -> task.name == "check" }.configureEach {
            dependsOn("dokkaGenerate")
        }
    }

    pluginManager.withPlugin("maven-publish") {
        extensions.configure<PublishingExtension> {
            publications.withType<MavenPublication>().configureEach {
                pom {
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
                    url = uri(stagingPublicationRepository)
                }
            }
        }
    }
}

tasks.register("dokkaGenerate") {
    group = "documentation"
    description = "Generates Dokka API documentation for every dependency-catalog module."
    dependsOn(
        subprojects.map { project ->
            "${project.path}:dokkaGenerate"
        },
    )
}

val checkDependencyCatalogArchitecture =
    tasks.register("checkDependencyCatalogArchitecture") {
        group = "verification"
        description = "Verifies the portable catalog API, core, and Gradle adapter."
        dependsOn(
            ":catalog-api:check",
            ":catalog-core:check",
            ":catalog-gradle-plugin:check",
            ":catalog-tree-gradle-plugin:check",
        )
    }

tasks.named("check") {
    dependsOn(checkDependencyCatalogArchitecture)
}

tasks.register("publishPortablePublicationToStagingRepository") {
    group = "publishing"
    description = "Publishes the reusable catalog API, core, and Gradle plugin to the staging repository."
    dependsOn(
        ":catalog-api:publishAllPublicationsToStagingRepository",
        ":catalog-core:publishAllPublicationsToStagingRepository",
        ":catalog-gradle-plugin:publishAllPublicationsToStagingRepository",
        ":catalog-tree-gradle-plugin:publishAllPublicationsToStagingRepository",
    )
}

val verifyProviderStagedPublication =
    tasks.register<Exec>("verifyProviderStagedPublication") {
        group = "verification"
        description = "Applies the staged provider-based catalog plugin from a source-independent consumer."
        dependsOn("publishPortablePublicationToStagingRepository")

        val sampleDirectory = layout.projectDirectory.dir("samples/standalone-consumer")
        val wrapper =
            layout.projectDirectory.file(
                if (System.getProperty("os.name").startsWith(
                        "Windows",
                        ignoreCase = true,
                    )
                ) {
                    "../../gradlew.bat"
                } else {
                    "../../gradlew"
                },
            )

        workingDir(sampleDirectory)
        commandLine(
            wrapper.asFile.absolutePath,
            "--no-daemon",
            "verifyCatalogs",
            "-PdependencyCatalogVersion=$publicationVersion",
            "-PdependencyCatalogPublicationRepository=$stagingPublicationRepository",
            "--stacktrace",
        )
    }

val verifyTreeStagedPublication =
    tasks.register<Exec>("verifyTreeStagedPublication") {
        group = "verification"
        description = "Applies the staged tree catalog plugin from a source-independent consumer."
        dependsOn("publishPortablePublicationToStagingRepository")

        val sampleDirectory = layout.projectDirectory.dir("samples/standalone-tree-consumer")
        val wrapper =
            layout.projectDirectory.file(
                if (System.getProperty("os.name").startsWith(
                        "Windows",
                        ignoreCase = true,
                    )
                ) {
                    "../../gradlew.bat"
                } else {
                    "../../gradlew"
                },
            )

        workingDir(sampleDirectory)
        commandLine(
            wrapper.asFile.absolutePath,
            "--no-daemon",
            "verifyCatalogs",
            "-PdependencyCatalogVersion=$publicationVersion",
            "-PdependencyCatalogPublicationRepository=$stagingPublicationRepository",
            "--stacktrace",
        )
    }

tasks.register("verifyStagedPublication") {
    group = "verification"
    description = "Verifies both staged dependency catalog settings adapters."
    dependsOn(
        verifyProviderStagedPublication,
        verifyTreeStagedPublication,
    )
}
