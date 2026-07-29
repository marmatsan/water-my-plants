package com.marmatsan.verificationPlatform.domain.model.errorhandling

/**
 * One product-source violation of the configured typed-result contract.
 *
 * @property relativePath repository-relative Kotlin source path.
 * @property lineNumber one-based line containing the violation.
 * @property reason actionable explanation of the rejected construct.
 */
data class TypedResultUsageViolation(
    val relativePath: String,
    val lineNumber: Int,
    val reason: String
)
