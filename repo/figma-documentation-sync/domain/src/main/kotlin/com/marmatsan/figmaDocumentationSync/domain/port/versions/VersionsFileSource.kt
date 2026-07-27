package com.marmatsan.figmaDocumentationSync.domain.port.versions

/**
 * Version properties file source expressed as a path for domain isolation.
 *
 * The consuming project selects the concrete file path.
 *
 * Example:
 * ```
 * VersionsFileSource("versions.properties")
 * ```
 *
 * @property path Path to the repository version properties file.
 */
data class VersionsFileSource(
    val path: String,
)
