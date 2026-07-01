package com.marmatsan.figmaDesignSync.domain.port.versions

/**
 * Version properties file source expressed as a path for domain isolation.
 *
 * In the current repository this points to `build-logic/versions.properties`.
 *
 * Example:
 * ```
 * VersionsFileSource("build-logic/versions.properties")
 * ```
 *
 * @property path Path to the repository version properties file.
 */
data class VersionsFileSource(
    val path: String
)
