package com.marmatsan.verificationPlatform.data.teamcity

import com.marmatsan.verificationPlatform.domain.model.ci.CiPlan
import com.marmatsan.verificationPlatform.domain.model.ci.CiPlanMode
import com.marmatsan.verificationPlatform.domain.model.ci.CiScope
import com.marmatsan.verificationPlatform.domain.model.ci.VerificationUnitId

/** Maps the provider-neutral plan to allow-listed TeamCity build parameters. */
class TeamCityCiPlanParameters {
    /**
     * Converts [plan] into the fixed TeamCity parameter contract.
     *
     * Only reviewed parameter names are emitted. Selected Gradle tasks are
     * validated against an identifier allow-list before becoming a parameter.
     *
     * @throws IllegalArgumentException when a selected task is not allow-listed.
     */
    fun create(
        plan: CiPlan,
    ): Map<String, String> =
        linkedMapOf(
            "ci.plan.schemaVersion" to plan.schemaVersion.toString(),
            "ci.plan.mode" to plan.mode.externalName(),
            "ci.plan.scope" to plan.scope.externalName(),
            "ci.plan.comparisonBase" to plan.comparisonBase.orEmpty(),
            "ci.plan.head" to plan.head,
            "ci.plan.fullVerification" to plan.fullVerification.toString(),
            "ci.plan.fallbackReason" to plan.fallbackReason.orEmpty(),
            "ci.plan.changedModules" to plan.changedModules.joinToString(","),
            "ci.plan.affectedModules" to plan.affectedModules.joinToString(","),
            "ci.plan.gradleTasks" to
                validatedGradleTasks(
                    tasks = plan.requiredGradleTasks(),
                ),
        ).apply {
            plan.verificationUnits.forEach { unit ->
                put(
                    "ci.unit.${unit.id.externalName()}.required",
                    unit.required.toString(),
                )
            }
        }

    private fun CiPlanMode.externalName(): String =
        when (this) {
            CiPlanMode.OBSERVATION -> "observation"
            CiPlanMode.ENFORCED -> "enforced"
        }

    private fun CiScope.externalName(): String =
        when (this) {
            CiScope.DOCUMENTATION_ONLY -> "documentation-only"
            CiScope.TEAMCITY -> "teamcity"
            CiScope.TOOLING -> "tooling"
            CiScope.BUILD_INFRASTRUCTURE -> "build-infrastructure"
            CiScope.APPLICATION -> "application"
            CiScope.MIXED -> "mixed"
            CiScope.UNKNOWN -> "unknown"
        }

    private fun VerificationUnitId.externalName(): String =
        when (this) {
            VerificationUnitId.GIT_WORKFLOW -> "git-workflow"
            VerificationUnitId.DOCUMENTATION -> "documentation"
            VerificationUnitId.REPOSITORY_DIFF -> "repository-diff"
            VerificationUnitId.TEAMCITY_DSL -> "teamcity-dsl"
            VerificationUnitId.TOOLING -> "tooling"
            VerificationUnitId.BUILD_INFRASTRUCTURE -> "build-infrastructure"
            VerificationUnitId.PORTABLE_DISTRIBUTION -> "portable-distribution"
            VerificationUnitId.GRADLE_VERIFICATION -> "gradle-verification"
            VerificationUnitId.PUBLISH_REPORTS -> "publish-reports"
        }

    private fun validatedGradleTasks(
        tasks: List<String>,
    ): String {
        require(tasks.all(GRADLE_TASK::matches)) {
            "The CI plan contains a Gradle task outside the TeamCity allow-list."
        }
        return tasks.joinToString(" ")
    }

    private companion object {
        val GRADLE_TASK =
            Regex(
                "^(?:(?::[A-Za-z0-9_.-]+)+:)?[A-Za-z][A-Za-z0-9_-]*$",
            )
    }
}
