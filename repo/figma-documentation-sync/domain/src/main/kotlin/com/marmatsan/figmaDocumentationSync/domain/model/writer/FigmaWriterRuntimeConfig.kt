package com.marmatsan.figmaDocumentationSync.domain.model.writer

import com.marmatsan.figmaDocumentationSync.domain.model.visual.CiVisualPlanConfig

/**
 * Small portable configuration required by runner generation and MCP execution.
 *
 * @property metadataPageId Figma page that owns canonical metadata.
 * @property metadataNamespace shared plugin-data namespace for canonical metadata.
 * @property figmaFileKey target Figma document key.
 * @property projectDisplayName human-readable project name used by the runner.
 * @property mcpClientName client identity sent to the MCP endpoint.
 * @property repositoryRootRelativeToTools path from writer tools to the repository root.
 * @property changeImpactPolicyRelativeToRepository repository-relative impact-policy path.
 * @property writerTargetNames all writer targets supported by the project adapter.
 * @property catalogTargetNames catalog targets rendered by the writer.
 * @property ciVisualPlanConfig optional CI visual planning configuration.
 * @property canonicalStagingNamespace namespace reserved for canonical payload staging.
 * @property visualTargetNames writer targets that perform visual changes.
 */
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
