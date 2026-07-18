package com.marmatsan.figmaDesignSync.domain.model.impact

/** Versioned path policy used to classify repository changes for Figma. */
data class FigmaChangeImpactPolicy(
    val documentationOnlyPaths: List<String>,
    val transportOnlyPaths: List<String>,
    val modelNeutralPaths: List<String>,
    val modelContentPaths: List<String>,
    val visualWriterPaths: List<String>,
    val visualTargetRules: List<FigmaVisualTargetRule>
)
