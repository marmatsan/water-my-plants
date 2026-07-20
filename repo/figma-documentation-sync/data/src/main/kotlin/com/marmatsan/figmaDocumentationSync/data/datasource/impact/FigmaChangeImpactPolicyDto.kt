package com.marmatsan.figmaDocumentationSync.data.datasource.impact

import kotlinx.serialization.Serializable

@Serializable
internal data class FigmaChangeImpactPolicyDto(
    val schemaVersion: Int,
    val documentationOnlyPaths: List<String>,
    val figmaTransportOnlyPaths: List<String>,
    val figmaModelNeutralPaths: List<String>,
    val figmaModelContentPaths: List<String>,
    val figmaVisualWriterPaths: List<String>,
    val figmaVisualTargetRules: List<VisualTargetRuleDto>,
) {
    @Serializable
    data class VisualTargetRuleDto(
        val paths: List<String>,
        val targets: List<String>,
    )
}
