package com.marmatsan.figmaCatalogChecks.plugin.checker.catalog

import java.io.File

internal data class FigmaBuildLogicCatalogTreeCheckRequest(
    val pageUrl: String,
    val sectionUrl: String,
    val settingsFile: File,
    val token: String
)
