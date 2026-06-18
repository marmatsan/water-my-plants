package com.marmatsan.figmaCatalogChecks.plugin.checker.catalog

import java.io.File

internal data class FigmaCatalogTreeCheckRequest(
    val pageUrl: String,
    val sectionUrl: String,
    val projectRootDir: File,
    val token: String
)
