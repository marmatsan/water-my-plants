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
    `kotlin-dsl` apply false
    alias(plugins.plugins.org.jetbrains.dokka) apply false
}

tasks.named("check") {
    dependsOn(
        ":data:check",
        ":domain:check",
        ":plugin:check",
        ":teamcity-adapter:check",
        ":teamcity-operations:check"
    )
}

tasks.register("dokkaGenerate") {
    group = "documentation"
    description = "Generates all portable Figma documentation sync API references."
    dependsOn(
        ":data:dokkaGenerate",
        ":domain:dokkaGenerate",
        ":plugin:dokkaGenerate",
        ":teamcity-adapter:dokkaGenerate",
        ":teamcity-operations:dokkaGenerate"
    )
}

@DisableCachingByDefault(
    because = "The verification task has no reusable output artifact"
)
abstract class VerifyPublicationVersionAlignmentTask : DefaultTask() {
    @get:Input
    abstract val mavenVersion: Property<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val packageJson: RegularFileProperty

    @TaskAction
    fun verifyVersions() {
        val npmVersion =
            Regex("\\\"version\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"")
                .find(packageJson.get().asFile.readText())
                ?.groupValues
                ?.get(
                    index = 1
                )
                ?: error("Missing version in tools/package.json")
        val expectedVersion = mavenVersion.get()
        check(npmVersion == expectedVersion) {
            "Publication version mismatch: Maven=$expectedVersion, npm=$npmVersion"
        }
    }
}

val publicationGroup: String =
    providers
        .gradleProperty("figmaDocumentationSyncGroup")
        .getOrElse("com.marmatsan.figma-documentation-sync")
val publicationVersion: String =
    providers
        .gradleProperty("figmaDocumentationSyncVersion")
        .getOrElse(
            Properties().run {
                file("versions.properties").inputStream().use(::load)
                getProperty("figmaDocumentationSyncVersion")
            }
        )
val configuredPublicationRepository: String? =
    providers
        .gradleProperty("figmaDocumentationSyncPublicationRepository")
        .orNull
val stagingPublicationRepository: String =
    configuredPublicationRepository
        ?: layout.buildDirectory
            .dir("publication-repository")
            .get()
            .asFile.absolutePath
allprojects {
    group = publicationGroup
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
            moduleName.convention("figmaDocumentationSync-${project.name}")

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
                        VisibilityModifier.Internal
                    )
                )
                reportUndocumented.set(true)

                val localSourceDirectory = layout.projectDirectory.dir("src/main/kotlin")
                if (localSourceDirectory.asFile.exists()) {
                    sourceLink {
                        localDirectory.set(localSourceDirectory)
                        remoteUrl.set(
                            URI(
                                "https://github.com/marmatsan/water-my-plants/tree/main/" +
                                    "repo/figma-documentation-sync/" +
                                    localSourceDirectory.asFile
                                        .relativeTo(rootProject.projectDir)
                                        .invariantSeparatorsPath
                            )
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
                    url.set("https://github.com/marmatsan/water-my-plants/tree/main/repo/figma-documentation-sync")
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

val verifyPublicationVersionAlignment =
    tasks.register<VerifyPublicationVersionAlignmentTask>(
        "verifyPublicationVersionAlignment"
    ) {
        group = "verification"
        description = "Checks that Maven and npm publication versions remain aligned."

        mavenVersion.set(publicationVersion)
        packageJson.set(layout.projectDirectory.file("tools/package.json"))
    }

tasks.register("publishPortablePublicationToStagingRepository") {
    group = "publishing"
    description = "Publishes the portable Figma sync artifacts to the configured staging Maven repository."

    dependsOn(
        verifyPublicationVersionAlignment,
        ":domain:publishAllPublicationsToStagingRepository",
        ":data:publishAllPublicationsToStagingRepository",
        ":plugin:publishAllPublicationsToStagingRepository",
        ":teamcity-adapter:publishAllPublicationsToStagingRepository",
        ":teamcity-operations:publishAllPublicationsToStagingRepository"
    )
}

tasks.register<Exec>("verifyStagedPublication") {
    group = "verification"
    description = "Applies the staged Figma plugin from a standalone consumer build."
    dependsOn("publishPortablePublicationToStagingRepository")

    val sampleDirectory = layout.projectDirectory.dir("samples/standalone-consumer")
    val wrapper =
        layout.projectDirectory.file(
            if (System.getProperty("os.name").startsWith(
                    "Windows",
                    ignoreCase = true
                )
            ) {
                "../../gradlew.bat"
            } else {
                "../../gradlew"
            }
        )

    workingDir(sampleDirectory)
    commandLine(
        wrapper.asFile.absolutePath,
        "--no-daemon",
        "--gradle-user-home",
        layout.buildDirectory
            .dir("gradle-user-home/standalone-consumer")
            .get()
            .asFile.absolutePath,
        "verifyPluginApplication",
        "-PfigmaDocumentationSyncVersion=$publicationVersion",
        "-PfigmaDocumentationSyncPublicationRepository=$stagingPublicationRepository",
        "--stacktrace"
    )
}
