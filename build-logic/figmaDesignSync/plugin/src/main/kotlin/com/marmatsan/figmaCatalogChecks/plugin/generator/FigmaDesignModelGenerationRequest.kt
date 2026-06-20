package com.marmatsan.figmaDesignSync.plugin.generator

import java.io.File
import java.time.Instant

internal data class FigmaDesignModelGenerationRequest(
    val branch: String,
    val gitSha: String,
    val generatedAt: Instant,
    val versionsFile: File,
    val rootSettingsFile: File,
    val buildLogicSettingsFile: File,
    val projectRootDirectory: File,
    val buildLogicRootDirectory: File
)
