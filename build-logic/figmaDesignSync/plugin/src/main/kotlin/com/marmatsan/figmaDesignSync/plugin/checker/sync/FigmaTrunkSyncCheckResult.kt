package com.marmatsan.figmaDesignSync.plugin.checker.sync

internal data class FigmaTrunkSyncCheckResult(
    val modelHash: String,
    val gitSha: String
)
