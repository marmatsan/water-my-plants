package com.marmatsan.figmaDocumentationSync.domain.model.impact

/** Deterministic Figma classification for one repository change set. */
data class FigmaChangeImpact(
    val scope: FigmaVerificationScope,
    val impact: FigmaImpact,
    val affectedVisualTargets: List<String>,
    val comparisonBase: String?,
    val changedPaths: List<String>
)
