package com.marmatsan.figmaDocumentationSync.plugin.task.official

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome

internal class OfficialFigmaSyncGradleTasksTest : FunSpec(
    {
    test("documentation-only scope skips model generation and metadata verification") {
        val project = Files.createTempDirectory("official-figma-sync-gradle").toFile()
        try {
            project.writeFixture()
            project.initializeGitRepository()

            val preparation = project.runner(
                "prepareOfficialFigmaSync",
                "-PfigmaChangedPaths=docs/example.md",
                "--stacktrace"
            ).build()
            val verification = project.runner(
                "verifyOfficialFigmaSync",
                "--stacktrace"
            ).build()
            val scope = Json.parseToJsonElement(
                project.resolve("build/reports/figma-sync/sync-scope.json").readText()
            ).jsonObject

            scope["scope"]?.jsonPrimitive?.content shouldBe "documentation-only"
            preparation.task(":materializeFigmaSyncCiConfiguration")?.outcome shouldBe TaskOutcome.SKIPPED
            preparation.task(":generateOfficialFigmaSyncModel")?.outcome shouldBe TaskOutcome.SKIPPED
            verification.task(":checkOfficialFigmaTrunkSync")?.outcome shouldBe TaskOutcome.SKIPPED
        } finally {
            project.deleteRecursively()
        }
    }

    test("TeamCity can execute official synchronization as visible sequential phases") {
        val project = Files.createTempDirectory("official-figma-sync-phases").toFile()
        try {
            project.writeFixture()
            project.initializeGitRepository()

            val property = "-PfigmaOfficialTeamCityPhasedExecution=true"
            project.runner(
                "classifyOfficialFigmaSyncChangeImpact",
                "-PfigmaChangedPaths=docs/example.md",
                property,
                "--stacktrace"
            ).build()
            val modelPhase = project.runner(
                "materializeFigmaSyncCiConfiguration",
                "generateOfficialFigmaSyncModel",
                property,
                "--stacktrace"
            ).build()
            val runnerPhase = project.runner(
                "prepareOfficialFigmaSync",
                property,
                "--stacktrace"
            ).build()
            project.runner(
                "validateOfficialFigmaSyncScope",
                property,
                "--stacktrace"
            ).build()
            val metadataPhase = project.runner(
                "checkOfficialFigmaTrunkSync",
                property,
                "--stacktrace"
            ).build()

            modelPhase.task(":classifyOfficialFigmaSyncChangeImpact") shouldBe null
            runnerPhase.task(":generateOfficialFigmaSyncModel") shouldBe null
            metadataPhase.task(":validateOfficialFigmaSyncScope") shouldBe null
            modelPhase.task(":materializeFigmaSyncCiConfiguration")?.outcome shouldBe TaskOutcome.SKIPPED
            modelPhase.task(":generateOfficialFigmaSyncModel")?.outcome shouldBe TaskOutcome.SKIPPED
            metadataPhase.task(":checkOfficialFigmaTrunkSync")?.outcome shouldBe TaskOutcome.SKIPPED
        } finally {
            project.deleteRecursively()
        }
    }
}
)

private fun File.runner(
    vararg arguments: String
): GradleRunner =
    GradleRunner.create()
        .withProjectDir(this)
        .withPluginClasspath()
        .withArguments(*arguments)

private fun File.writeFixture() {
    resolve("settings.gradle.kts").writeText("rootProject.name = \"official-figma-sync-test\"")
    resolve("build.gradle.kts").writeText(
        """
        plugins {
            id("com.marmatsan.figmaDocumentationSync")
        }

        figmaDocumentationSync {
            changeImpactPolicyFile.set(
                layout.projectDirectory.file("project-config/change-impact-policy.json")
            )
        }
        """.trimIndent()
    )
    resolve("project-config").mkdirs()
    resolve("project-config/change-impact-policy.json").writeText(
        """
        {
          "schemaVersion": 1,
          "documentationOnlyPaths": ["docs/*.md"],
          "figmaTransportOnlyPaths": [],
          "figmaModelNeutralPaths": [],
          "figmaModelContentPaths": ["settings.gradle.kts"],
          "figmaVisualWriterPaths": [],
          "figmaVisualTargetRules": []
        }
        """.trimIndent()
    )
}

private fun File.initializeGitRepository() {
    git("init")
    git(
        "checkout",
        "-b",
        "main"
    )
    git(
        "add",
        "."
    )
    git(
        "-c",
        "user.name=Test",
        "-c",
        "user.email=test@example.com",
        "commit",
        "-m",
        "Fixture"
    )
}

private fun File.git(
    vararg arguments: String
) {
    val process = ProcessBuilder(listOf("git") + arguments)
        .directory(this)
        .start()
    val error = ByteArrayOutputStream()
    process.errorStream.use { input -> input.copyTo(error) }
    check(process.waitFor() == 0) {
        "Failed to run git ${arguments.joinToString(" ")}: ${error.toString().trim()}"
    }
}
