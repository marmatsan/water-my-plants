package com.marmatsan.verificationPlatform.domain.model

/**
 * Changed modules, their reverse dependents, and any graph validation failure.
 *
 * @property changedModules modules that directly own one or more changed paths.
 * @property affectedModules changed modules plus all transitive reverse
 * dependents that must be verified.
 * @property fallbackReason graph validation failure requiring full repository
 * verification, or `null` for a valid calculation.
 */
data class ModuleImpact(
    val changedModules: List<String>,
    val affectedModules: List<String>,
    val fallbackReason: String?,
) {
    /** Whether module impact can be used for targeted verification. */
    val isValid: Boolean
        get() = fallbackReason == null
}
