package com.marmatsan.figmaDocumentationSync.plugin.task.canonical

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files

internal class CanonicalFigmaSyncGradleTasksTest :
    FunSpec(
        {
            test("documentation-only scope skips model generation and metadata verification") {
                val project = Files.createTempDirectory("canonical-figma-sync-gradle").toFile()
                try {
                    project.writeFixture()
                    project.initializeGitRepository()

                    val preparation =
                        project
                            .runner(
                                "prepareCanonicalFigmaSync",
                                "-PfigmaChangedPaths=docs/example.md",
                                "--stacktrace",
                            ).build()
                    val verification =
                        project
                            .runner(
                                "verifyCanonicalFigmaSync",
                                "--stacktrace",
                            ).build()
                    val scope =
                        Json
                            .parseToJsonElement(
                                project
                                    .resolve(
                                        relative = "build/reports/figma-sync/sync-scope.json",
                                    ).readText(),
                            ).jsonObject

                    scope["scope"]?.jsonPrimitive?.content shouldBe "documentation-only"
                    preparation.task(":materializeFigmaSyncCiConfiguration")?.outcome shouldBe TaskOutcome.SKIPPED
                    preparation.task(":generateCanonicalFigmaSyncModel")?.outcome shouldBe TaskOutcome.SKIPPED
                    verification.task(":checkCanonicalFigmaTrunkSync")?.outcome shouldBe TaskOutcome.SKIPPED
                } finally {
                    project.deleteRecursively()
                }
            }

            test("TeamCity can execute canonical synchronization as visible sequential phases") {
                val project = Files.createTempDirectory("canonical-figma-sync-phases").toFile()
                try {
                    project.writeFixture()
                    project.initializeGitRepository()

                    val property = "-PfigmaCanonicalTeamCityPhasedExecution=true"
                    project
                        .runner(
                            "classifyCanonicalFigmaSyncChangeImpact",
                            "-PfigmaChangedPaths=docs/example.md",
                            property,
                            "--stacktrace",
                        ).build()
                    val modelPhase =
                        project
                            .runner(
                                "materializeFigmaSyncCiConfiguration",
                                "generateCanonicalFigmaSyncModel",
                                property,
                                "--stacktrace",
                            ).build()
                    val runnerPhase =
                        project
                            .runner(
                                "prepareCanonicalFigmaSync",
                                property,
                                "--stacktrace",
                            ).build()
                    project
                        .runner(
                            "validateCanonicalFigmaSyncScope",
                            property,
                            "--stacktrace",
                        ).build()
                    val metadataPhase =
                        project
                            .runner(
                                "checkCanonicalFigmaTrunkSync",
                                property,
                                "--stacktrace",
                            ).build()

                    modelPhase.task(":classifyCanonicalFigmaSyncChangeImpact") shouldBe null
                    runnerPhase.task(":generateCanonicalFigmaSyncModel") shouldBe null
                    metadataPhase.task(":validateCanonicalFigmaSyncScope") shouldBe null
                    modelPhase.task(":materializeFigmaSyncCiConfiguration")?.outcome shouldBe TaskOutcome.SKIPPED
                    modelPhase.task(":generateCanonicalFigmaSyncModel")?.outcome shouldBe TaskOutcome.SKIPPED
                    metadataPhase.task(":checkCanonicalFigmaTrunkSync")?.outcome shouldBe TaskOutcome.SKIPPED
                } finally {
                    project.deleteRecursively()
                }
            }
        },
    )

private fun File.runner(
    vararg arguments: String,
): GradleRunner =
    GradleRunner
        .create()
        .withProjectDir(this)
        .withPluginClasspath()
        .withArguments(*arguments)

private fun File.writeFixture() {
    resolve(
        relative = "settings.gradle.kts",
    ).writeText("rootProject.name = \"canonical-figma-sync-test\"")
    resolve(
        relative = "build.gradle.kts",
    ).writeText(
        """
        plugins {
            id("com.marmatsan.figmaDocumentationSync")
        }

        figmaDocumentationSync {
            changeImpactPolicyFile.set(
                layout.projectDirectory.file("project-config/change-impact-policy.json")
            )
        }
        """.trimIndent(),
    )
    resolve(
        relative = "project-config",
    ).mkdirs()
    resolve(
        relative = "project-config/change-impact-policy.json",
    ).writeText(
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
        "user.name=Test",
        "-c",
        "user.email=test@example.com",
        "commit",
        "-m",
        "Fixture",
    )
}

private fun File.git(
    vararg arguments: String,
) {
    val process =
        ProcessBuilder(listOf("git") + arguments)
            .directory(this)
            .start()
    val error = ByteArrayOutputStream()
    process.errorStream.use { input -> input.copyTo(error) }
    check(process.waitFor() == 0) {
        "Failed to run git ${arguments.joinToString(" ")}: ${error.toString().trim()}"
    }
}
