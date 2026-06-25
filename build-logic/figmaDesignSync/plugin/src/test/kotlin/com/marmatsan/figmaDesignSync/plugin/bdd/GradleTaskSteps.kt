package com.marmatsan.figmaDesignSync.plugin.bdd

import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome

class GradleTaskSteps {

    private lateinit var projectDir: File
    private lateinit var designModelFile: File

    @Given("a temporary Gradle project exists")
    fun aTemporaryGradleProjectExists() {
        projectDir = Files.createTempDirectory("figma-design-sync-bdd").toFile()
        designModelFile = projectDir.resolve("build/reports/figma-sync/design-model.json")
    }

    @Given("the temporary Gradle project has repository model files")
    fun theTemporaryGradleProjectHasRepositoryModelFiles() {
        projectDir.writeRepositoryModelFiles()
    }

    @Given("the temporary Gradle project applies the figmaDesignSync plugin")
    fun theTemporaryGradleProjectAppliesTheFigmaDesignSyncPlugin() {
        projectDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("com.marmatsan.figmaDesignSync")
            }
            """.trimIndent()
        )
    }

    @Given("the temporary Gradle project is a git repository")
    fun theTemporaryGradleProjectIsAGitRepository() {
        projectDir.initializeGitRepository()
    }

    @When("generateFigmaDesignModel runs in the temporary project")
    fun generateFigmaDesignModelRunsInTheTemporaryProject() {
        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("generateFigmaDesignModel", "--stacktrace")
            .build()

        result.task(":generateFigmaDesignModel")?.outcome shouldBe TaskOutcome.SUCCESS
    }

    @Then("the design model report is written in the temporary project")
    fun theDesignModelReportIsWrittenInTheTemporaryProject() {
        designModelFile.shouldExist()
    }

    @Then("the written design model contains the current branch")
    fun theWrittenDesignModelContainsTheCurrentBranch() {
        writtenDesignModel()["branch"]?.jsonPrimitive?.content shouldBe "main"
    }

    @Then("the written design model contains the current git sha")
    fun theWrittenDesignModelContainsTheCurrentGitSha() {
        writtenDesignModel()["gitSha"]?.jsonPrimitive?.content shouldBe projectDir.git("rev-parse", "HEAD")
    }

    @Then("the written design model contains content")
    fun theWrittenDesignModelContainsContent() {
        writtenDesignModel()["content"]!!.jsonObject.keys shouldContainAll listOf(
            "versions",
            "versionSections",
            "catalogs",
            "modules",
            "moduleDependencies"
        )
    }

    @Then("the written design model contains a model hash")
    fun theWrittenDesignModelContainsAModelHash() {
        writtenDesignModel().keys shouldContainAll listOf("modelHash")
    }

    private fun writtenDesignModel() =
        Json.parseToJsonElement(designModelFile.readText()).jsonObject

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
        resolve("build-logic").mkdirs()
        resolve("build-logic/settings.gradle.kts").writeText(
            """
            dependencyResolutionManagement {
                versionCatalogs {
                    create("libs") {
                    }
                    create("plugins") {
                    }
                }
            }
            """.trimIndent()
        )
        resolve("build-logic/versions.properties").writeText(
            """
            ## Main project dependencies
            kotlinVersion=2.4.0
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
