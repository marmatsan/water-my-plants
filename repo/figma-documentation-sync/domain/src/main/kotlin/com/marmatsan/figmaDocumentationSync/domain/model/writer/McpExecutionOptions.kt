package com.marmatsan.figmaDocumentationSync.domain.model.writer

/**
 * Selection flags applied before an MCP runner is executed.
 *
 * @property resume whether to continue a compatible checkpoint.
 * @property retryFailed whether the previously failed atomic file may run again.
 * @property reuseStaging whether completed canonical staging may be reused.
 * @property from optional runner file at which execution begins.
 */
data class McpExecutionOptions(
    val resume: Boolean = false,
    val retryFailed: Boolean = false,
    val reuseStaging: Boolean = false,
    val from: String? = null,
)
