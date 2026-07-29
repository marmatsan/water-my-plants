package com.marmatsan.figmaDocumentationSync.domain.port.ci

/**
 * Generated CI configuration and the adapter selected to read it.
 *
 * @property directoryPath repository-relative generated configuration directory.
 * @property providerClassName project-selected provider implementation class.
 */
data class CiGeneratedConfigurationSource(
    val directoryPath: String,
    val providerClassName: String
)
