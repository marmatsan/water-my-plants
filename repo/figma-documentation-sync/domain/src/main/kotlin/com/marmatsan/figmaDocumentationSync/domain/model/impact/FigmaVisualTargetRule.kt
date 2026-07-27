package com.marmatsan.figmaDocumentationSync.domain.model.impact

/**
 * Maps visual writer paths to the smallest Figma targets they can affect.
 *
 * @property paths repository path patterns evaluated by the classifier.
 * @property targets focused writer targets selected by a match.
 */
data class FigmaVisualTargetRule(
    val paths: List<String>,
    val targets: List<String>,
)
