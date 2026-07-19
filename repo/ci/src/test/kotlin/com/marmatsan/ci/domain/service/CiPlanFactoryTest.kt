package com.marmatsan.ci.domain.service

import com.marmatsan.ci.domain.model.CiScope
import com.marmatsan.ci.domain.model.CiPlanMode
import com.marmatsan.ci.domain.model.RepositoryChangeSet
import com.marmatsan.ci.domain.model.VerificationUnitId
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
}) {
    companion object {
        private fun plan(vararg paths: String) = CiPlanFactory().create(
            RepositoryChangeSet(
                comparisonBase = "base-sha",
                head = "head-sha",
                changedFiles = paths.toList()
            )
        )

        private fun com.marmatsan.ci.domain.model.CiPlan.requiredUnitIds() =
            verificationUnits.filter { it.required }.map { it.id }

        private fun com.marmatsan.ci.domain.model.CiPlan.gradleTasks() =
            verificationUnits.flatMap { it.gradleTasks }
    }
}
