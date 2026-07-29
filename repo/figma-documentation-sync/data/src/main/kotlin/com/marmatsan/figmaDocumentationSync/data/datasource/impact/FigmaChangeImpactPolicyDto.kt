package com.marmatsan.figmaDocumentationSync.data.datasource.impact

import kotlinx.serialization.Serializable

/**
 * Serialization boundary for the versioned repository impact policy.
 *
 * @property schemaVersion policy schema version.
 * @property documentationOnlyPaths documentation-only path patterns.
 * @property figmaTransportOnlyPaths transport-only path patterns.
 * @property figmaModelNeutralPaths model-neutral path patterns.
 * @property figmaModelContentPaths model-content path patterns.
 * @property figmaVisualWriterPaths visual writer path patterns.
 * @property figmaVisualTargetRules focused visual target rules.
 */
@Serializable
internal data class FigmaChangeImpactPolicyDto(
    val schemaVersion: Int,
    val documentationOnlyPaths: List<String>,
    val figmaTransportOnlyPaths: List<String>,
    val figmaModelNeutralPaths: List<String>,
    val figmaModelContentPaths: List<String>,
    val figmaVisualWriterPaths: List<String>,
    val figmaVisualTargetRules: List<VisualTargetRuleDto>
) {
    /**
     * Serialized path-to-visual-target rule.
     *
     * @property paths repository path patterns.
     * @property targets focused visual targets selected by a match.
     */
    @Serializable
    data class VisualTargetRuleDto(
        val paths: List<String>,
        val targets: List<String>
    )
}
