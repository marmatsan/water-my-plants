package com.marmatsan.verificationPlatform.domain.model.errorhandling

/**
 * Expected failure produced when source files violate one typed-result policy.
 *
 * @property violations non-empty violations discovered in the inspected source.
 */
data class TypedResultUsageError(
    val violations: List<TypedResultUsageViolation>
) {
    init {
        require(violations.isNotEmpty()) {
            "A typed Result usage error must contain at least one violation."
        }
    }
}
