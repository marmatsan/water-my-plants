package com.marmatsan.figmaDocumentationSync.domain.port.ci

/**
 * Versioned external topology YAML source expressed as a path.
 *
 * @property path repository-relative topology source path.
 */
data class CiExternalTopologySource(
    val path: String
)
