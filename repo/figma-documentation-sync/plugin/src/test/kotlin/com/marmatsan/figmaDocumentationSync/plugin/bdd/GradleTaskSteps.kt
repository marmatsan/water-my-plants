package com.marmatsan.figmaDocumentationSync.plugin.bdd

import io.cucumber.java8.En
import io.cucumber.java8.HookNoArgsBody
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files

class GradleTaskSteps : En {
    private lateinit var projectDir: File
    private lateinit var designModelFile: File
    private lateinit var result: BuildResult
    private var canonicalFigmaSyncGenerationAuthorized = false

    init {
        Given("a temporary Gradle project exists") {
            projectDir = Files.createTempDirectory("figma-documentation-sync-bdd").toFile()
            designModelFile =
                projectDir.resolve(
                    relative = "build/reports/figma-sync/design-model.json",
                )
            canonicalFigmaSyncGenerationAuthorized = false
        }

        Given("the temporary Gradle project has repository model files") {
            projectDir.writeRepositoryModelFiles()
        }

        Given("the temporary Gradle project applies the figmaDocumentationSync plugin") {
            projectDir.writeBuildFile(
                ciDocumentationEnabled = true,
            )
        }

        Given("the temporary Gradle project applies the figmaDocumentationSync plugin without CI documentation") {
            projectDir.writeBuildFile(
                ciDocumentationEnabled = false,
            )
        }

        Given("the temporary Gradle project has no CI documentation inputs") {
            projectDir
                .resolve(
                    relative = "docs/ci",
                ).deleteRecursively()
            projectDir
                .resolve(
                    relative = ".teamcity",
                ).deleteRecursively()
        }

        Given("the temporary Gradle project is a git repository") {
            projectDir.initializeGitRepository()
        }

        Given("canonical Figma Sync model generation is authorized") {
            canonicalFigmaSyncGenerationAuthorized = true
        }

        When("generateFigmaDesignModel runs in the temporary project") {
            result = gradleRunner().build()

            result.task(":generateFigmaDesignModel")?.outcome shouldBe TaskOutcome.SUCCESS
        }

        When("generateFigmaDesignModel runs without canonical Figma Sync authorization") {
            result = gradleRunner().buildAndFail()
        }

        Then("the design model report is written in the temporary project") {
            designModelFile.shouldExist()
        }

        Then("generateFigmaDesignModel fails because canonical Figma Sync generation is required") {
            result.output shouldContain "Missing FIGMA_DOCUMENTATION_SYNC_CANONICAL=true"
        }

        Then("the written design model contains the current branch") {
            writtenDesignModel()["branch"]?.jsonPrimitive?.content shouldBe "main"
        }

        Then("the written design model contains the current git sha") {
            writtenDesignModel()["gitSha"]?.jsonPrimitive?.content shouldBe
                projectDir.git(
                    "rev-parse",
                    "HEAD",
                )
        }

        Then("the written design model contains content") {
            writtenDesignModel()["content"]!!.jsonObject.keys shouldContainAll
                listOf(
                    "versions",
                    "versionSections",
                    "catalogs",
                    "modules",
                    "moduleDependencies",
                    "ci",
                )
        }

        Then("the written design model contains portable content without CI") {
            val contentKeys = writtenDesignModel()["content"]!!.jsonObject.keys

            contentKeys shouldContainAll
                listOf(
                    "versions",
                    "versionSections",
                    "catalogs",
                    "modules",
                    "moduleDependencies",
                )
            contentKeys.contains("ci") shouldBe false
        }

        Then("the written design model contains repository infrastructure modules") {
            val modules =
                writtenDesignModel()["content"]!!
                    .jsonObject["modules"]!!
                    .jsonArray
                    .map { module -> module.jsonPrimitive.content }

            modules shouldContainAll
                listOf(
                    ":dependency-catalog:catalog-api",
                    ":dependency-catalog:catalog-core",
                    ":dependency-catalog:catalog-gradle-plugin",
                    ":figma-documentation-sync:data",
                    ":figma-documentation-sync:domain",
                    ":figma-documentation-sync:plugin",
                    ":gradle-plugins:android",
                )

            val dependencies =
                writtenDesignModel()["content"]!!
                    .jsonObject["moduleDependencies"]!!
                    .jsonObject["dependencyCatalog"]!!
                    .jsonArray
                    .map { dependency ->
                        val dependencyObject = dependency.jsonObject
                        dependencyObject["dependentModule"]!!.jsonPrimitive.content to
                            dependencyObject["dependencyModule"]!!.jsonPrimitive.content
                    }

            dependencies shouldBe
                listOf(
                    ":dependency-catalog:catalog-gradle-plugin" to
                        ":dependency-catalog:catalog-api",
                )
        }

        Then("the written design model contains a model hash") {
            writtenDesignModel().keys shouldContainAll listOf("modelHash")
        }

        After(
            "@gradle",
            HookNoArgsBody {
                if (::projectDir.isInitialized) {
                    projectDir.deleteRecursively()
                }
            },
        )
    }

    private fun writtenDesignModel() =
        Json.parseToJsonElement(designModelFile.readText()).jsonObject

    private fun gradleRunner(): GradleRunner =
        GradleRunner
            .create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments(
                "generateFigmaDesignModel",
                "--stacktrace",
            ).withEnvironment(gradleEnvironment())

    private fun gradleEnvironment(): Map<String, String> =
        System.getenv().toMutableMap().apply {
            if (canonicalFigmaSyncGenerationAuthorized) {
                put(
                    "FIGMA_DOCUMENTATION_SYNC_CANONICAL",
                    "true",
                )
                put(
                    "FIGMA_DOCUMENTATION_SYNC_BRANCH",
                    "main",
                )
            } else {
                remove("FIGMA_DOCUMENTATION_SYNC_CANONICAL")
                remove("FIGMA_DOCUMENTATION_SYNC_BRANCH")
            }
        }

    private fun File.writeBuildFile(
        ciDocumentationEnabled: Boolean,
    ) {
        val ciDocumentationConfiguration =
            if (ciDocumentationEnabled) {
                """
                ciDocumentationEnabled.set(true)
                ciConfigurationModelName.set("teamCity")
                ciConfigurationProviderClassName.set(EmptyCiConfigurationProvider::class.java.name)
                ciExternalTopologyFile.set(layout.projectDirectory.file("docs/ci/external-topology.yaml"))
                ciWindowsRuntimeFile.set(layout.projectDirectory.file("docs/ci/windows-runtime.yaml"))
                ciGeneratedConfigurationDirectory.set(
                    layout.projectDirectory.dir(".teamcity/target/generated-configs")
                )
                """.trimIndent()
            } else {
                ""
            }

        resolve(
            relative = "build.gradle.kts",
        ).writeText(
            """
            import com.marmatsan.figmaDocumentationSync.data.ci.configuration.EmptyCiConfigurationProvider
            import com.marmatsan.figmaDocumentationSync.data.dependencies.catalog.EmptyDependencyDslCatalogProvider

            plugins {
                id("com.marmatsan.figmaDocumentationSync")
            }

            figmaDocumentationSync {
                primaryCatalogModelName.set("fixture")
                dependencyCatalogProviderClassName.set(EmptyDependencyDslCatalogProvider::class.java.name)
                versionsFile.set(layout.projectDirectory.file("versions.properties"))
                $ciDocumentationConfiguration

                includedBuilds.register("dependency-catalog") {
                    modelName.set("dependencyCatalog")
                    settingsFile.set(
                        layout.projectDirectory.file("repo/dependency-catalog/settings.gradle.kts")
                    )
                    rootDirectory.set(layout.projectDirectory.dir("repo/dependency-catalog"))
                    modulePathPrefix.set(":dependency-catalog")
                    publishesCatalogs.set(false)
                }
                includedBuilds.register("figma-documentation-sync") {
                    modelName.set("figmaDocumentationSync")
                    settingsFile.set(
                        layout.projectDirectory.file("repo/figma-documentation-sync/settings.gradle.kts")
                    )
                    rootDirectory.set(layout.projectDirectory.dir("repo/figma-documentation-sync"))
                    modulePathPrefix.set(":figma-documentation-sync")
                }
                includedBuilds.register("gradle-plugins") {
                    modelName.set("gradlePlugins")
                    settingsFile.set(
                        layout.projectDirectory.file("repo/gradle-plugins/settings.gradle.kts")
                    )
                    rootDirectory.set(layout.projectDirectory.dir("repo/gradle-plugins"))
                    modulePathPrefix.set(":gradle-plugins")
                    publishesConventionPlugins.set(true)
                }
            }
            """.trimIndent(),
        )
    }

    private fun File.writeRepositoryModelFiles() {
        resolve(
            relative = "settings.gradle.kts",
        ).writeText(
            """
            pluginManagement {
                repositories {
                    google()
                    mavenCentral()
                    gradlePluginPortal()
                }
            }

            dependencyResolutionManagement {
                repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
                repositories {
                    google()
                    mavenCentral()
                }
            }

            rootProject.name = "figma-documentation-sync-bdd"
            include(":app")
            include(":core:ui")
            """.trimIndent(),
        )
        resolve(
            relative = "app",
        ).mkdirs()
        resolve(
            relative = "app/build.gradle.kts",
        ).writeText("")
        resolve(
            relative = "core/ui",
        ).mkdirs()
        resolve(
            relative = "core/ui/build.gradle.kts",
        ).writeText("")
        resolve(
            relative = "repo/gradle-plugins",
        ).mkdirs()
        resolve(
            relative = "repo/dependency-catalog",
        ).mkdirs()
        resolve(
            relative = "repo/figma-documentation-sync",
        ).mkdirs()
        resolve(
            relative = "repo/gradle-plugins/settings.gradle.kts",
        ).writeText(
            """
            dependencyResolutionManagement {
                versionCatalogs {
                    create("libs") {
                    }
                    create("plugins") {
                    }
                }
            }

            include(":android")
            """.trimIndent(),
        )
        resolve(
            relative = "repo/gradle-plugins/android",
        ).mkdirs()
        resolve(
            relative = "repo/gradle-plugins/android/build.gradle.kts",
        ).writeText("")
        resolve(
            relative = "repo/dependency-catalog/settings.gradle.kts",
        ).writeText(
            """
            rootProject.name = "dependency-catalog"
            enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
            include(":catalog-api", ":catalog-core", ":catalog-gradle-plugin")
            """.trimIndent(),
        )
        resolve(
            relative = "repo/dependency-catalog/catalog-api",
        ).mkdirs()
        resolve(
            relative = "repo/dependency-catalog/catalog-api/build.gradle.kts",
        ).writeText("")
        resolve(
            relative = "repo/dependency-catalog/catalog-core",
        ).mkdirs()
        resolve(
            relative = "repo/dependency-catalog/catalog-core/build.gradle.kts",
        ).writeText("")
        resolve(
            relative = "repo/dependency-catalog/catalog-gradle-plugin",
        ).mkdirs()
        resolve(
            relative = "repo/dependency-catalog/catalog-gradle-plugin/build.gradle.kts",
        ).writeText(
            """
            dependencies {
                implementation(projects.catalogApi)
            }
            """.trimIndent(),
        )
        resolve(
            relative = "versions.properties",
        ).writeText(
            """
            ## Main project dependencies
            androidGradlePluginVersion=9.2.1
            kotlinVersion=2.4.0
            """.trimIndent(),
        )
        resolve(
            relative = "repo/figma-documentation-sync/settings.gradle.kts",
        ).writeText(
            """
            rootProject.name = "figma-documentation-sync"

            dependencyResolutionManagement {
                versionCatalogs {
                    create("libs") {
                    }
                    create("plugins") {
                    }
                }
            }

            include(":data", ":domain", ":plugin")
            """.trimIndent(),
        )
        resolve(
            relative = "repo/figma-documentation-sync/data",
        ).mkdirs()
        resolve(
            relative = "repo/figma-documentation-sync/data/build.gradle.kts",
        ).writeText("")
        resolve(
            relative = "repo/figma-documentation-sync/domain",
        ).mkdirs()
        resolve(
            relative = "repo/figma-documentation-sync/domain/build.gradle.kts",
        ).writeText("")
        resolve(
            relative = "repo/figma-documentation-sync/plugin",
        ).mkdirs()
        resolve(
            relative = "repo/figma-documentation-sync/plugin/build.gradle.kts",
        ).writeText("")
        resolve(
            relative = "docs/ci",
        ).mkdirs()
        resolve(
            relative = "docs/ci/external-topology.yaml",
        ).writeText(
            """
            schemaVersion: 1
            validation:
              lastValidatedOn: "2026-07-14"
              warnAfterDays: 90
            nodes:
              - id: operator
                type: actor
                name: Operator
                description: Initiates manual CI actions.
            connections: []
            """.trimIndent(),
        )
        resolve(
            relative = "docs/ci/windows-runtime.yaml",
        ).writeText(
            """
            schemaVersion: 1
            validation:
              lastValidatedOn: "2026-07-16"
              warnAfterDays: 90
            platform: Windows
            services:
              - id: teamcity-server
                name: TeamCity Server
                description: Hosts TeamCity.
                service: TeamCity
                startup: Automatic
                identity: NT SERVICE\TeamCity
            """.trimIndent(),
        )
        resolve(
            relative = ".teamcity/target/generated-configs/Root_Ci",
        ).mkdirs()
        resolve(
            relative = ".teamcity/target/generated-configs/Root_Ci/project-config.xml",
        ).writeText(
            """
            <project>
              <name>CI</name>
            </project>
            """.trimIndent(),
        )
        resolve(
            relative = ".teamcity/target/generated-configs/Root_Ci/pipeline.yml",
        ).writeText(
            """
            version: 1
            jobs:
              verify:
                name: Verify
            """.trimIndent(),
        )
    }

    private fun File.initializeGitRepository() {
        git("init")
        git(
            "checkout",
            "-b",
            "main",
        )
        git(
            "add",
            ".",
        )
        git(
            "-c",
            "user.name=BDD Test",
            "-c",
            "user.email=bdd@example.com",
            "commit",
            "-m",
            "Initial fixture",
        )
    }

    private fun File.git(
        vararg arguments: String,
    ): String {
        val process =
            ProcessBuilder(listOf("git") + arguments)
                .directory(this)
                .start()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        process.inputStream.use { input -> input.copyTo(output) }
        process.errorStream.use { input -> input.copyTo(error) }
        val exitValue = process.waitFor()

        check(exitValue == 0) {
            "Failed to run git ${arguments.joinToString(" ")}: ${error.toString().trim()}"
        }

        return output.toString().trim()
    }
}
