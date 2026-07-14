package com.marmatsan.figmaDesignSync.plugin.bdd

import io.cucumber.java8.En
import io.cucumber.java8.HookNoArgsBody
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome

class GradleTaskSteps : En {

    private lateinit var projectDir: File
    private lateinit var designModelFile: File
    private lateinit var result: BuildResult
    private var officialFigmaSyncGenerationAuthorized = false

    init {
        Given("a temporary Gradle project exists") {
            projectDir = Files.createTempDirectory("figma-design-sync-bdd").toFile()
            designModelFile = projectDir.resolve("build/reports/figma-sync/design-model.json")
            officialFigmaSyncGenerationAuthorized = false
        }

        Given("the temporary Gradle project has repository model files") {
            projectDir.writeRepositoryModelFiles()
        }

        Given("the temporary Gradle project applies the figmaDesignSync plugin") {
            projectDir.resolve("build.gradle.kts").writeText(
                """
                plugins {
                    id("com.marmatsan.figmaDesignSync")
                }
                """.trimIndent()
            )
        }

        Given("the temporary Gradle project is a git repository") {
            projectDir.initializeGitRepository()
        }

        Given("official Figma Sync model generation is authorized") {
            officialFigmaSyncGenerationAuthorized = true
        }

        When("generateFigmaDesignModel runs in the temporary project") {
            result = gradleRunner().build()

            result.task(":generateFigmaDesignModel")?.outcome shouldBe TaskOutcome.SUCCESS
        }

        When("generateFigmaDesignModel runs without official Figma Sync authorization") {
            result = gradleRunner().buildAndFail()
        }

        Then("the design model report is written in the temporary project") {
            designModelFile.shouldExist()
        }

        Then("generateFigmaDesignModel fails because official Figma Sync generation is required") {
            result.output shouldContain "Missing FIGMA_DESIGN_SYNC_OFFICIAL=true"
        }

        Then("the written design model contains the current branch") {
            writtenDesignModel()["branch"]?.jsonPrimitive?.content shouldBe "main"
        }

        Then("the written design model contains the current git sha") {
            writtenDesignModel()["gitSha"]?.jsonPrimitive?.content shouldBe projectDir.git("rev-parse", "HEAD")
        }

        Then("the written design model contains content") {
            writtenDesignModel()["content"]!!.jsonObject.keys shouldContainAll listOf(
                "versions",
                "versionSections",
                "catalogs",
                "modules",
                "moduleDependencies",
                "ci"
            )
        }

        Then("the written design model contains repository infrastructure modules") {
            val modules = writtenDesignModel()["content"]!!
                .jsonObject["modules"]!!
                .jsonArray
                .map { module -> module.jsonPrimitive.content }

            modules shouldContainAll listOf(
                ":dependency-catalog:catalog-core",
                ":dependency-catalog:water-my-plants-catalog",
                ":figma-design-sync:data",
                ":figma-design-sync:domain",
                ":figma-design-sync:plugin",
                ":gradle-plugins:android"
            )

            val dependencies = writtenDesignModel()["content"]!!
                .jsonObject["moduleDependencies"]!!
                .jsonObject["dependencyCatalog"]!!
                .jsonArray
                .map { dependency ->
                    val dependencyObject = dependency.jsonObject
                    dependencyObject["dependentModule"]!!.jsonPrimitive.content to
                        dependencyObject["dependencyModule"]!!.jsonPrimitive.content
                }

            dependencies shouldBe listOf(
                ":dependency-catalog:water-my-plants-catalog" to
                    ":dependency-catalog:catalog-core"
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
            }
        )
    }

    private fun writtenDesignModel() =
        Json.parseToJsonElement(designModelFile.readText()).jsonObject

    private fun gradleRunner(): GradleRunner =
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("generateFigmaDesignModel", "--stacktrace")
            .withEnvironment(gradleEnvironment())

    private fun gradleEnvironment(): Map<String, String> =
        System.getenv().toMutableMap().apply {
            if (officialFigmaSyncGenerationAuthorized) {
                put("FIGMA_DESIGN_SYNC_OFFICIAL", "true")
                put("FIGMA_DESIGN_SYNC_BRANCH", "main")
            } else {
                remove("FIGMA_DESIGN_SYNC_OFFICIAL")
                remove("FIGMA_DESIGN_SYNC_BRANCH")
            }
        }

    private fun File.writeRepositoryModelFiles() {
        resolve("settings.gradle.kts").writeText(
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

            rootProject.name = "figma-design-sync-bdd"
            include(":app")
            include(":core:ui")
            """.trimIndent()
        )
        resolve("app").mkdirs()
        resolve("app/build.gradle.kts").writeText("")
        resolve("core/ui").mkdirs()
        resolve("core/ui/build.gradle.kts").writeText("")
        resolve("repo/gradle-plugins").mkdirs()
        resolve("repo/dependency-catalog").mkdirs()
        resolve("repo/figma-design-sync").mkdirs()
        resolve("repo/gradle-plugins/settings.gradle.kts").writeText(
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
            """.trimIndent()
        )
        resolve("repo/gradle-plugins/android").mkdirs()
        resolve("repo/gradle-plugins/android/build.gradle.kts").writeText("")
        resolve("repo/dependency-catalog/settings.gradle.kts").writeText(
            """
            rootProject.name = "dependency-catalog"
            enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
            include(":catalog-core", ":water-my-plants-catalog")
            """.trimIndent()
        )
        resolve("repo/dependency-catalog/catalog-core").mkdirs()
        resolve("repo/dependency-catalog/catalog-core/build.gradle.kts").writeText("")
        resolve("repo/dependency-catalog/water-my-plants-catalog").mkdirs()
        resolve("repo/dependency-catalog/water-my-plants-catalog/build.gradle.kts").writeText(
            """
            dependencies {
                implementation(projects.catalogCore)
            }
            """.trimIndent()
        )
        resolve("repo/dependency-catalog/versions.properties").writeText(
            """
            ## Main project dependencies
            androidGradlePluginVersion=9.2.1
            kotlinVersion=2.4.0
            """.trimIndent()
        )
        resolve("repo/figma-design-sync/settings.gradle.kts").writeText(
            """
            rootProject.name = "figma-design-sync"

            dependencyResolutionManagement {
                versionCatalogs {
                    create("libs") {
                    }
                    create("plugins") {
                    }
                }
            }

            include(":data", ":domain", ":plugin")
            """.trimIndent()
        )
        resolve("repo/figma-design-sync/data").mkdirs()
        resolve("repo/figma-design-sync/data/build.gradle.kts").writeText("")
        resolve("repo/figma-design-sync/domain").mkdirs()
        resolve("repo/figma-design-sync/domain/build.gradle.kts").writeText("")
        resolve("repo/figma-design-sync/plugin").mkdirs()
        resolve("repo/figma-design-sync/plugin/build.gradle.kts").writeText("")
        resolve("docs/ci").mkdirs()
        resolve("docs/ci/external-topology.yaml").writeText(
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
            """.trimIndent()
        )
        resolve(".teamcity/target/generated-configs/Root_Ci").mkdirs()
        resolve(".teamcity/target/generated-configs/Root_Ci/project-config.xml").writeText(
            """
            <project>
              <name>CI</name>
            </project>
            """.trimIndent()
        )
        resolve(".teamcity/target/generated-configs/Root_Ci/pipeline.yml").writeText(
            """
            version: 1
            jobs:
              verify:
                name: Verify
            """.trimIndent()
        )
    }

    private fun File.initializeGitRepository() {
        git("init")
        git("checkout", "-b", "main")
        git("add", ".")
        git("-c", "user.name=BDD Test", "-c", "user.email=bdd@example.com", "commit", "-m", "Initial fixture")
    }

    private fun File.git(vararg arguments: String): String {
        val process = ProcessBuilder(listOf("git") + arguments)
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
