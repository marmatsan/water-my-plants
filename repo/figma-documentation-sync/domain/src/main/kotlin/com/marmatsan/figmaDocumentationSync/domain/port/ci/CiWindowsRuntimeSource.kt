package com.marmatsan.figmaDocumentationSync.domain.port.ci

/**
 * Location of the repository-owned Windows CI runtime YAML.
 *
 * @property filePath repository-relative Windows runtime source path.
 */
data class CiWindowsRuntimeSource(
    val filePath: String,
)
