package com.marmatsan.figmaDocumentationSync.domain.port.versions

/**
 * Version properties file source expressed as a path for domain isolation.
 *
 * In the current repository this points to `repo/dependency-catalog/versions.properties`.
 *
 * Example:
 * ```
 * VersionsFileSource("repo/dependency-catalog/versions.properties")
 * ```
 *
 * @property path Path to the repository version properties file.
 */
data class VersionsFileSource(
    val path: String,
)
