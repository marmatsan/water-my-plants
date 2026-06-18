package com.marmatsan.figmaCatalogChecks.plugin.checker.modules

import java.io.File

internal data class FigmaModulesCheckRequest(
    val pageUrl: String,
    val moduleComponentUrl: String,
    val rootSettingsFile: File,
    val buildLogicSettingsFile: File,
    val token: String
)
