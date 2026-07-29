package com.marmatsan.figmaDocumentationSync.plugin.checker.versions

/**
 * Result of checking repository version key naming.
 *
 * @property violations naming violations in deterministic validation order.
 */
internal data class VersionNamingCheckResult(
    val violations: List<VersionNamingViolation>
) {
    /** Whether all version sections and keys satisfy the repository naming contract. */
    val isSuccessful: Boolean
        get() = violations.isEmpty()
}

/**
 * One version naming rule violation.
 *
 * @property message actionable description of the violated naming rule.
 */
internal data class VersionNamingViolation(
    val message: String
)
