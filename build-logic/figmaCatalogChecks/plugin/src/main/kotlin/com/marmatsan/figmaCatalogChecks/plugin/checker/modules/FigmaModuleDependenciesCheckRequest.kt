package com.marmatsan.figmaCatalogChecks.plugin.checker.modules

import java.io.File

internal data class FigmaModuleDependenciesCheckRequest(
    val pageUrl: String,
    val mainSectionUrl: String,
    val buildLogicSectionUrl: String,
    val projectRootDirectory: File,
    val buildLogicRootDirectory: File,
    val token: String
)
