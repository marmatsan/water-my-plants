package com.marmatsan.figmaDocumentationSync.plugin.checker.versions

import java.io.File

/**
 * Input for checking repository version key naming.
 *
 * @property versionsFile source versions file selected by the consuming project.
 */
internal data class VersionNamingCheckRequest(
    val versionsFile: File
)
