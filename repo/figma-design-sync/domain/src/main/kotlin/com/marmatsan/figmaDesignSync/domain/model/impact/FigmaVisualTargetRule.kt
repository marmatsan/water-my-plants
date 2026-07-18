package com.marmatsan.figmaDesignSync.domain.model.impact

/** Maps visual writer paths to the smallest Figma targets they can affect. */
data class FigmaVisualTargetRule(
    val paths: List<String>,
    val targets: List<String>
)
