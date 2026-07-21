package com.marmatsan.figmaDocumentationSync.domain.model.writer

import com.marmatsan.figmaDocumentationSync.domain.model.visual.CiVisualPlanConfig

/** Small portable configuration required by runner generation and MCP execution. */
data class FigmaWriterRuntimeConfig(
    val metadataPageId: String,
    val metadataNamespace: String,
    val figmaFileKey: String,
    val projectDisplayName: String,
    val mcpClientName: String,
    val repositoryRootRelativeToTools: String,
    val changeImpactPolicyRelativeToRepository: String,
    val writerTargetNames: List<String>,
    val catalogTargetNames: List<String>,
    val ciVisualPlanConfig: CiVisualPlanConfig? = null,
) {
    val canonicalStagingNamespace: String = "${metadataNamespace}_staging"
    val visualTargetNames: List<String> = writerTargetNames.filterNot { target -> target == "metadata" }
}
