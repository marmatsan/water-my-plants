package com.marmatsan.verificationPlatform.domain.model.ci

import kotlinx.serialization.Serializable

/**
 * Provider-neutral verification selected for one repository revision.
 *
 * [verificationUnits] is the executable contract consumed by provider
 * adapters. [fullVerification] and [fallbackReason] make conservative
 * fail-closed decisions explicit and reviewable.
 *
 * @property schemaVersion version of this serialized plan contract.
 * @property mode whether adapters observe or enforce the selected plan.
 * @property comparisonBase Git revision used as the lower comparison boundary,
 * or `null` when a provider cannot supply one.
 * @property head Git revision being verified.
 * @property scope highest-level classification of the committed change.
 * @property changedFiles normalized repository-relative paths in the diff.
 * @property changedModules modules that directly own changed non-documentation
 * paths.
 * @property affectedModules changed modules plus transitive reverse dependents.
 * @property verificationUnits allow-listed work and dependency metadata for CI
 * adapters.
 * @property fullVerification whether the root `check` must replace targeted
 * module verification.
 * @property fallbackReason conservative-classification explanation, or `null`
 * when no fallback was needed.
 */
@Serializable
data class CiPlan(
    val schemaVersion: Int,
    val mode: CiPlanMode,
    val comparisonBase: String?,
    val head: String,
    val scope: CiScope,
    val changedFiles: List<String>,
    val changedModules: List<String>,
    val affectedModules: List<String>,
    val verificationUnits: List<VerificationUnit>,
    val fullVerification: Boolean,
    val fallbackReason: String?
) {
    /**
     * Returns the ordered, de-duplicated Gradle entry points for every required
     * verification unit.
     */
    fun requiredGradleTasks(): List<String> =
        verificationUnits
            .asSequence()
            .filter(VerificationUnit::required)
            .flatMap { unit -> unit.gradleTasks.asSequence() }
            .distinct()
            .toList()
}
