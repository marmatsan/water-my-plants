package com.marmatsan.figmaDocumentationSync.domain.model.impact

/**
 * Versioned path policy used to classify repository changes for Figma.
 *
 * @property documentationOnlyPaths paths that affect prose but not generated artifacts.
 * @property transportOnlyPaths paths that affect transport without changing model semantics.
 * @property modelNeutralPaths paths verified not to affect the generated model.
 * @property modelContentPaths paths that require full model verification.
 * @property visualWriterPaths paths that can change visual writer behavior.
 * @property visualTargetRules path rules selecting focused visual targets.
 */
data class FigmaChangeImpactPolicy(
    val documentationOnlyPaths: List<String>,
    val transportOnlyPaths: List<String>,
    val modelNeutralPaths: List<String>,
    val modelContentPaths: List<String>,
    val visualWriterPaths: List<String>,
    val visualTargetRules: List<FigmaVisualTargetRule>
)
