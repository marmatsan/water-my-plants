package com.marmatsan.figmaDesignSync.plugin.checker.versions

import java.io.File

/**
 * Input for checking repository version key naming.
 *
 * @property versionsFile Source `repo/dependency-catalog/versions.properties` file.
 */
internal data class VersionNamingCheckRequest(
    val versionsFile: File
)
