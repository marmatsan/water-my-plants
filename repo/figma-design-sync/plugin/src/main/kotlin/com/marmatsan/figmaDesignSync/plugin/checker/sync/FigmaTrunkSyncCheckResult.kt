package com.marmatsan.figmaDesignSync.plugin.checker.sync

/**
 * Metadata read from Figma after a successful trunk sync check.
 */
internal data class FigmaTrunkSyncCheckResult(
    val modelHash: String,
    val gitSha: String
)
