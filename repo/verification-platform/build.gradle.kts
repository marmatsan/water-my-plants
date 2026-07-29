@file:Suppress("AvoidDuplicateDependencies")

import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import java.net.URI
import java.util.Properties

plugins {
    base
    alias(plugins.plugins.org.jetbrains.kotlin.jvm) apply false
    alias(plugins.plugins.org.jetbrains.kotlin.plugin.serialization) apply false
    alias(plugins.plugins.org.jetbrains.dokka) apply false
}

val versions: Properties =
    Properties().apply {
        file("versions.properties").inputStream().use(::load)
    }
val publicationVersion: String =
    providers.gradleProperty("verificationPlatformVersion").getOrElse(
        versions.getProperty("verificationPlatformVersion"),
    )
val stagingPublicationRepository: String =
    providers.gradleProperty("verificationPlatformPublicationRepository").orNull
        ?: layout.buildDirectory
            .dir("publication-repository")
            .get()
            .asFile.absolutePath

allprojects {
    group = "com.marmatsan.verification-platform"
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
            moduleName.convention("verification-platform-${project.name}")

            dokkaPublications.configureEach {
                failOnWarning.set(true)

                val moduleReadme = layout.projectDirectory.file("docs/dokka/README.md")
                if (moduleReadme.asFile.exists()) {
                    includes.from(moduleReadme)
                }
            }

            dokkaSourceSets.configureEach {
                documentedVisibilities.set(
                    setOf(
                        VisibilityModifier.Public,
                        VisibilityModifier.Internal,
                    ),
                )
                reportUndocumented.set(true)

                val localSourceDirectory = layout.projectDirectory.dir("src/main/kotlin")
                if (localSourceDirectory.asFile.exists()) {
                    sourceLink {
                        localDirectory.set(localSourceDirectory)
                        remoteUrl.set(
                            URI(
                                "https://github.com/marmatsan/water-my-plants/tree/main/" +
                                    "repo/verification-platform/" +
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
    dependsOn(
        ":domain:check",
        ":data:check",
        ":plugin:check",
    )
}

tasks.register("dokkaGenerate") {
    group = "documentation"
    description = "Generates the CI domain, data, and plugin API documentation."
    dependsOn(
        ":domain:dokkaGenerate",
        ":data:dokkaGenerate",
        ":plugin:dokkaGenerate",
    )
}

tasks.register("publishPortablePublicationToStagingRepository") {
    group = "publishing"
    description = "Publishes verification domain, adapters, plugin, and plugin marker."
    dependsOn(
        ":domain:publishAllPublicationsToStagingRepository",
        ":data:publishAllPublicationsToStagingRepository",
        ":plugin:publishAllPublicationsToStagingRepository",
    )
}

tasks.register<Exec>("verifyStagedPublication") {
    group = "verification"
    description = "Applies the staged verification plugin from a source-independent consumer."
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
        "verifyPluginApplication",
        "-PverificationPlatformVersion=$publicationVersion",
        "-PverificationPlatformPublicationRepository=$stagingPublicationRepository",
        "--stacktrace",
    )
}
