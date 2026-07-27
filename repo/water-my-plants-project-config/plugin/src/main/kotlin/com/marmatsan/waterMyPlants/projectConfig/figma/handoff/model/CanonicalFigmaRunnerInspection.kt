package com.marmatsan.waterMyPlants.projectConfig.figma.handoff.model

internal data class CanonicalFigmaRunnerInspection(
    val manifestHash: String,
    val statePath: String,
    val reuseStaging: Boolean,
    val decision: String?,
    val executionFiles: List<String>,
)
