package com.marmatsan.figmaDocumentationSync.plugin.checker.impact

import com.marmatsan.figmaDocumentationSync.domain.model.impact.FigmaChangeImpactPolicy
import com.marmatsan.figmaDocumentationSync.domain.model.impact.FigmaImpact
import com.marmatsan.figmaDocumentationSync.domain.model.impact.FigmaVerificationScope
import com.marmatsan.figmaDocumentationSync.domain.model.impact.FigmaVisualTargetRule
import com.marmatsan.figmaDocumentationSync.domain.model.impact.RepositoryChangeSet
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class FigmaChangeImpactClassifierTest : FunSpec({
    val classifier = FigmaChangeImpactClassifier()

    test("documentation changes do not require Gradle or Figma verification") {
        val result = classifier.classify(
            changeSet("docs/documentation.md", "core/ui/docs/README.md"),
            policy()
        )

        result.scope shouldBe FigmaVerificationScope.DOCUMENTATION_ONLY
        result.impact shouldBe FigmaImpact.DOCUMENTATION_ONLY
        result.affectedVisualTargets shouldBe emptyList()
    }

    test("transport changes do not request a visual rewrite") {
        val result = classifier.classify(
            changeSet(
                "repo/figma-documentation-sync/data/src/main/kotlin/com/marmatsan/figmaDocumentationSync/data/mcp/McpRunnerExecutor.kt",
                "repo/figma-documentation-sync/docs/runbooks/mcp-chunk-transport.md"
            ),
            policy()
        )

        result.scope shouldBe FigmaVerificationScope.TRANSPORT_ONLY
        result.impact shouldBe FigmaImpact.TRANSPORT_ONLY
    }

    test("model-neutral tooling still requires normal repository verification") {
        val result = classifier.classify(
            changeSet(
                "repo/verification-platform/plugin/src/main/kotlin/com/marmatsan/verificationPlatform/plugin/CheckDocumentationTask.kt",
                "docs/ci/documentation-coverage.md"
            ),
            policy()
        )

        result.scope shouldBe FigmaVerificationScope.MODEL_NEUTRAL
        result.impact shouldBe FigmaImpact.MODEL_NEUTRAL
    }

    test("a mapped visual writer selects only its configured targets") {
        val result = classifier.classify(
            changeSet(
                "repo/figma-documentation-sync/tools/src/figma/figma-version-sync-gateway.ts",
                "repo/figma-documentation-sync/docs/reference/visual-sync-contract.md"
            ),
            policy()
        )

        result.scope shouldBe FigmaVerificationScope.FULL_VERIFICATION
        result.impact shouldBe FigmaImpact.VISUAL_TARGETS
        result.affectedVisualTargets shouldBe listOf("versions")
    }

    test("an unmapped visual writer fails closed to all targets") {
        val result = classifier.classify(
            changeSet("repo/figma-documentation-sync/tools/src/usecases/sync-figma-design-model.ts"),
            policy()
        )

        result.impact shouldBe FigmaImpact.VISUAL_TARGETS
        result.affectedVisualTargets shouldBe listOf("all")
    }

    test("model sources require full model verification") {
        val result = classifier.classify(changeSet("app/build.gradle.kts"), policy())

        result.scope shouldBe FigmaVerificationScope.FULL_VERIFICATION
        result.impact shouldBe FigmaImpact.MODEL_CONTENT
    }

    test("unknown paths require full verification") {
        val result = classifier.classify(changeSet("gradle.properties"), policy())

        result.scope shouldBe FigmaVerificationScope.FULL_VERIFICATION
        result.impact shouldBe FigmaImpact.UNKNOWN
    }
})

private fun changeSet(vararg paths: String) = RepositoryChangeSet(
    comparisonBase = "base-sha",
    changedPaths = paths.toList()
)

private fun policy() = FigmaChangeImpactPolicy(
    documentationOnlyPaths = listOf("docs/*.md", "*/docs/*.md", "*/*/docs/*.md"),
    transportOnlyPaths = listOf(
        "repo/figma-documentation-sync/data/src/main/kotlin/com/marmatsan/figmaDocumentationSync/data/mcp/*"
    ),
    modelNeutralPaths = listOf("repo/verification-platform/*"),
    modelContentPaths = listOf("*/build.gradle.kts"),
    visualWriterPaths = listOf("repo/figma-documentation-sync/tools/src/*"),
    visualTargetRules = listOf(
        FigmaVisualTargetRule(
            paths = listOf("repo/figma-documentation-sync/tools/src/figma/figma-version-*"),
            targets = listOf("versions")
        )
    )
)
