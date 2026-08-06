@file:Suppress("AvoidDuplicateDependencies")

import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.configure
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import java.util.Properties

plugins {
    base
    `kotlin-dsl` apply false
    alias(plugins.plugins.org.jetbrains.dokka) apply false
}

val publicationVersion: String =
    providers
        .gradleProperty("projectConfigVersion")
        .getOrElse(
            Properties().run {
                file("versions.properties").inputStream().use(::load)
                getProperty("projectConfigVersion")
            }
        )
val stagingPublicationRepository: String =
    providers
        .gradleProperty("projectConfigPublicationRepository")
        .orNull
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
    }

    pluginManager.withPlugin("org.jetbrains.dokka") {
        extensions.configure<DokkaExtension> {
            moduleName.convention("project-config-${project.name}")
            dokkaPublications.configureEach {
                failOnWarning.set(true)
                includes.from(layout.projectDirectory.file("docs/dokka/README.md"))
            }
            dokkaSourceSets.configureEach {
                documentedVisibilities.set(
                    setOf(
                        VisibilityModifier.Public,
                        VisibilityModifier.Internal
                    )
                )
                reportUndocumented.set(true)
            }
        }
        tasks.named("check") {
            dependsOn("dokkaGenerate")
        }
    }

    pluginManager.withPlugin("maven-publish") {
        extensions.configure<PublishingExtension> {
            repositories {
                maven {
                    name = "staging"
                    url = uri(stagingPublicationRepository)
                }
            }
        }
    }
}

tasks.named("check") {
    dependsOn(":plugin:check")
}

tasks.register("dokkaGenerate") {
    group = "documentation"
    description = "Generates the reusable project-config API reference."
    dependsOn(":plugin:dokkaGenerate")
}

tasks.register("publishPortablePublicationToStagingRepository") {
    group = "publishing"
    description = "Publishes the reusable project-config Gradle plugins to staging."
    dependsOn(":plugin:publishAllPublicationsToStagingRepository")
}
