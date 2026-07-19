package com.marmatsan.ci.domain.service

import com.marmatsan.ci.domain.model.CiPlanMode
import com.marmatsan.ci.domain.model.CiScope
import com.marmatsan.ci.domain.model.ModuleDependency
import com.marmatsan.ci.domain.model.RepositoryChangeSet
import com.marmatsan.ci.domain.model.RepositoryModuleGraph
import com.marmatsan.ci.domain.model.VerificationUnitId
import com.marmatsan.ci.testModuleGraph
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

class CiPlanFactoryTest : FunSpec({
    test("documentation-only changes avoid full Gradle verification") {
        val plan = plan("docs/ci/main-branch-protection.md", "repo/ci/README.md")

        plan.scope shouldBe CiScope.DOCUMENTATION_ONLY
        plan.mode shouldBe CiPlanMode.ENFORCED
        plan.fullVerification shouldBe false
        plan.requiredUnitIds() shouldContain VerificationUnitId.REPOSITORY_DIFF
        plan.requiredUnitIds().contains(VerificationUnitId.GRADLE_VERIFICATION) shouldBe false
    }

    test("TeamCity changes retain full verification in enforced mode") {
        val plan = plan(".teamcity/settings.kts")

        plan.scope shouldBe CiScope.TEAMCITY
        plan.fullVerification shouldBe true
        plan.requiredUnitIds() shouldContain VerificationUnitId.TEAMCITY_DSL
        plan.gradleTasks() shouldBe listOf("check")
    }

    test("mixed changes keep all matching units") {
        val plan = plan("repo/figma-design-sync/tools/package.json", "app/build.gradle.kts")

        plan.scope shouldBe CiScope.MIXED
        plan.requiredUnitIds() shouldContain VerificationUnitId.FIGMA_TOOLING
        plan.requiredUnitIds() shouldContain VerificationUnitId.GRADLE_VERIFICATION
    }

    test("unknown paths fail closed") {
        val plan = plan("automation/unclassified.txt")

        plan.scope shouldBe CiScope.UNKNOWN
        plan.fullVerification shouldBe true
        plan.fallbackReason shouldBe "At least one changed path has no targeted verification policy."
        plan.gradleTasks() shouldBe listOf("check")
    }

    test("empty change sets fail closed") {
        val plan = plan()

        plan.scope shouldBe CiScope.UNKNOWN
        plan.fullVerification shouldBe true
        plan.fallbackReason shouldBe "No changed files were resolved; verification fails closed."
    }

    test("markdown outside an approved documentation surface is verified as code") {
        val plan = plan("repo/figma-design-sync/tools/implementation-notes.md")

        plan.scope shouldBe CiScope.FIGMA_TOOLING
        plan.fullVerification shouldBe true
        plan.requiredUnitIds() shouldContain VerificationUnitId.FIGMA_TOOLING
        plan.requiredUnitIds() shouldContain VerificationUnitId.GRADLE_VERIFICATION
    }

    test("application module changes select only the module and repository-wide catalog usage") {
        val plan = plan("app/src/main/kotlin/com/marmatsan/MainActivity.kt")

        plan.scope shouldBe CiScope.APPLICATION
        plan.changedModules shouldBe listOf(":app")
        plan.affectedModules shouldBe listOf(":app")
        plan.fullVerification shouldBe false
        plan.gradleTasks() shouldBe listOf(":app:check", "checkFigmaCatalogUsage")
    }

    test("core module changes include all transitive reverse dependents") {
        val plan = plan("core/ui/src/main/kotlin/com/marmatsan/ui/Theme.kt")

        plan.changedModules shouldBe listOf(":core:ui")
        plan.affectedModules shouldBe listOf(":app", ":core:ui", ":onboarding:ui")
        plan.gradleTasks() shouldBe listOf(
            ":app:check",
            ":core:ui:check",
            ":onboarding:ui:check",
            "checkFigmaCatalogUsage"
        )
    }

    test("unresolved module graph dependencies fail closed to root check") {
        val invalidGraph = testModuleGraph().copy(
            dependencies = testModuleGraph().dependencies + ModuleDependency(
                dependentModule = ":app",
                dependencyModule = ":missing"
            )
        )
        val plan = plan(
            "app/src/main/kotlin/com/marmatsan/MainActivity.kt",
            moduleGraph = invalidGraph
        )

        plan.scope shouldBe CiScope.UNKNOWN
        plan.changedModules shouldBe emptyList()
        plan.affectedModules shouldBe emptyList()
        plan.fullVerification shouldBe true
        plan.gradleTasks() shouldBe listOf("check")
        plan.fallbackReason shouldBe
            "The Gradle module graph contains an unresolved dependency: :app -> :missing."
    }
}) {
    companion object {
        private fun plan(
            vararg paths: String,
            moduleGraph: RepositoryModuleGraph = testModuleGraph()
        ) = CiPlanFactory().create(
            changeSet = RepositoryChangeSet(
                comparisonBase = "base-sha",
                head = "head-sha",
                changedFiles = paths.toList()
            ),
            moduleGraph = moduleGraph
        )

        private fun com.marmatsan.ci.domain.model.CiPlan.requiredUnitIds() =
            verificationUnits.filter { it.required }.map { it.id }

        private fun com.marmatsan.ci.domain.model.CiPlan.gradleTasks() =
            verificationUnits.flatMap { it.gradleTasks }
    }
}
