package com.marmatsan.figmaDesignSync.plugin.checker.sync

import java.io.File
import java.time.Instant

internal data class FigmaTrunkSyncCheckRequest(
    val metadataNodeUrl: String,
    val token: String,
    val branch: String,
    val gitSha: String,
    val generatedAt: Instant,
    val versionsFile: File,
    val rootSettingsFile: File,
    val buildLogicSettingsFile: File,
    val projectRootDirectory: File,
    val buildLogicRootDirectory: File
)
