package com.marmatsan.figmaDocumentationSync.plugin.checker.versions

/**
 * Result of checking repository version key naming.
 */
internal data class VersionNamingCheckResult(
    val violations: List<VersionNamingViolation>,
) {
    val isSuccessful: Boolean
        get() = violations.isEmpty()
}

/**
 * One version naming rule violation.
 */
internal data class VersionNamingViolation(
    val message: String,
)
