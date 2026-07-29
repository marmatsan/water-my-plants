package com.marmatsan.figmaDocumentationSync.domain.model.impact

/**
 * Deterministic Figma classification for one repository change set.
 *
 * @property scope verification depth required by the change.
 * @property impact highest detected kind of Figma impact.
 * @property affectedVisualTargets smallest visual targets selected by matching rules.
 * @property comparisonBase Git revision used as the comparison base.
 * @property changedPaths normalized repository-relative changed paths.
 */
data class FigmaChangeImpact(
    val scope: FigmaVerificationScope,
    val impact: FigmaImpact,
    val affectedVisualTargets: List<String>,
    val comparisonBase: String?,
    val changedPaths: List<String>
)
