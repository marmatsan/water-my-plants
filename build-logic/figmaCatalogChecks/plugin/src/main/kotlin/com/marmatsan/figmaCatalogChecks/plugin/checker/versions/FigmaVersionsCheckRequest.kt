package com.marmatsan.figmaCatalogChecks.plugin.checker.versions

import java.io.File

internal data class FigmaVersionsCheckRequest(
    val pageUrl: String,
    val sectionUrl: String,
    val versionComponentUrl: String,
    val versionsFile: File,
    val token: String
)
