package com.marmatsan.figmaDesignSync.data.fingerprint

import com.marmatsan.figmaDesignSync.domain.model.impact.FigmaChangeImpactPolicy
import com.marmatsan.figmaDesignSync.domain.model.impact.FigmaVisualTargetRule
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.nio.file.Files
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

internal class WriterScopeFingerprintCalculatorTest : FunSpec({
    test("target-specific changes invalidate only their execution family") {
        val root = Files.createTempDirectory("writer-fingerprints")
        try {
            val sourceRoot = root.resolve("repo/tools/src").apply { createDirectories() }
            val ciSource = sourceRoot.resolve("figma/figma-ci-gateway.ts").apply {
                parent.createDirectories()
                writeText("export const ci = 1;\n")
            }
            sourceRoot.resolve("figma/figma-catalog-gateway.ts").writeText("export const catalog = 1;\n")
            sourceRoot.resolve("figma/figma-node-gateway.ts").writeText("export const shared = 1;\n")
            sourceRoot.resolve("preview.ts").writeText("export const preview = 1;\n")

            val before = fingerprints(root, sourceRoot)
            ciSource.writeText("export const ci = 2;\n")
            val after = fingerprints(root, sourceRoot)

            after["ci.overview"] shouldNotBe before["ci.overview"]
            after["preflight"] shouldBe before["preflight"]
            after["versions"] shouldBe before["versions"]
            after["waterMyPlants.libraries.androidx"] shouldBe
                before["waterMyPlants.libraries.androidx"]
            after["metadata"] shouldBe before["metadata"]
            after["waterMyPlants.libraries.androidx"] shouldBe
                after["waterMyPlants.libraries.cleanup"]
        } finally {
            root.toFile().deleteRecursively()
        }
    }

    test("includes Kotlin visual planners outside the TypeScript source root") {
        val root = Files.createTempDirectory("kotlin-writer-fingerprints")
        try {
            val sourceRoot = root.resolve("repo/tools/src").apply { createDirectories() }
            sourceRoot.resolve("shared.ts").writeText("export const shared = 1;\n")
            val kotlinPlanner = root.resolve("repo/visual/CiVisualPlanner.kt").apply {
                parent.createDirectories()
                writeText("class CiVisualPlanner\n")
            }

            val before = fingerprints(root, sourceRoot)
            kotlinPlanner.writeText("class CiVisualPlannerV2\n")
            val after = fingerprints(root, sourceRoot)

            after["ci.overview"] shouldNotBe before["ci.overview"]
            after["versions"] shouldBe before["versions"]
        } finally {
            root.toFile().deleteRecursively()
        }
    }
})

private fun fingerprints(repositoryRoot: java.nio.file.Path, sourceRoot: java.nio.file.Path) =
    WriterScopeFingerprintCalculator().create(
        sourceRoot = sourceRoot,
        repositoryRoot = repositoryRoot,
        policy = FigmaChangeImpactPolicy(
            documentationOnlyPaths = emptyList(),
            transportOnlyPaths = listOf("repo/tools/src/preview.ts"),
            modelNeutralPaths = emptyList(),
            modelContentPaths = emptyList(),
            visualWriterPaths = listOf("repo/tools/src/*", "repo/visual/*"),
            visualTargetRules = listOf(
                FigmaVisualTargetRule(
                    paths = listOf("repo/tools/src/figma/figma-ci-*"),
                    targets = listOf("ci.overview")
                ),
                FigmaVisualTargetRule(
                    paths = listOf("repo/tools/src/figma/figma-catalog-*"),
                    targets = listOf("preflight", "waterMyPlants.libraries")
                ),
                FigmaVisualTargetRule(
                    paths = listOf("repo/visual/*"),
                    targets = listOf("ci.overview")
                )
            )
        ),
        writerTargets = listOf(
            "preflight",
            "versions",
            "waterMyPlants.libraries",
            "ci.overview",
            "metadata"
        ),
        catalogTargets = listOf("waterMyPlants.libraries"),
        scopes = listOf(
            "preflight",
            "versions",
            "waterMyPlants.libraries.androidx",
            "waterMyPlants.libraries.cleanup",
            "ci.overview"
        )
    )
