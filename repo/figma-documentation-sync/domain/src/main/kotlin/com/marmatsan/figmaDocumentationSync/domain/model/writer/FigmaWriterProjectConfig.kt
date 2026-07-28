package com.marmatsan.figmaDocumentationSync.domain.model.writer

/**
 * Typed repository configuration consumed by the portable Figma writer.
 *
 * @property metadataPageId Figma page that owns repository sync metadata.
 * @property metadataNamespace shared-plugin-data namespace for repository metadata.
 * @property figmaFileKey target Figma document key.
 * @property projectDisplayName human-readable project name rendered in documentation.
 * @property mcpClientName client identity sent to the MCP endpoint.
 * @property ciDocumentationPageId Figma page that owns CI documentation.
 * @property ciNodeComponentId component used to render one CI node.
 * @property ciIconComponentSetId component set used for CI environment icons.
 * @property ciVariableCollectionName variable collection containing CI theme values.
 * @property ciVariableModeNames allowed modes in the CI variable collection.
 * @property ciNodeInstanceName canonical name assigned to CI node instances.
 * @property ciIconInstanceName canonical name assigned to CI icon instances.
 * @property ciIconEnvironmentProperty icon property that selects an environment.
 * @property ciIconEnvironments supported icon environment values.
 * @property ciConnectorName canonical name assigned to generated CI connectors.
 * @property ciConnectorTemplateName Figma connector template name.
 * @property ciConnectorTemplateNodeId node id of the connector template.
 * @property ciNodeProps semantic-to-Figma property mapping for CI nodes.
 * @property ciNodePhaseContainerName layer that contains CI phases.
 * @property ciNodeOutcomeContainerName layer that contains CI outcomes.
 * @property ciPhaseComponentId component used to render a CI phase.
 * @property ciPhaseSlotNamePrefix prefix shared by phase slot properties.
 * @property ciPhaseSlotCount number of phase slots exposed by the component.
 * @property ciPhaseProps semantic-to-Figma property mapping for phases.
 * @property ciPhaseStepContainerName layer that contains phase steps.
 * @property ciStepComponentSetId component set used to render CI steps.
 * @property ciStepSlotNamePrefix prefix shared by step slot properties.
 * @property ciStepSlotCount number of step slots exposed by the component.
 * @property ciStepRoles supported semantic roles for CI steps.
 * @property ciStepProps semantic-to-Figma property mapping for steps.
 * @property ciOutcomeComponentSetId component set used to render CI outcomes.
 * @property ciOutcomeSlotNamePrefix prefix shared by outcome slot properties.
 * @property ciOutcomeSlotCount number of outcome slots exposed by the component.
 * @property ciOutcomeKinds supported semantic outcome kinds.
 * @property ciOutcomeProps semantic-to-Figma property mapping for outcomes.
 * @property versionsCollectionName variable collection containing dependency versions.
 * @property versionAliasModeName variable mode containing version aliases.
 * @property versionNumberModeName variable mode containing resolved version numbers.
 * @property outlineColorVariableName semantic outline color used by generated sections.
 * @property surfaceColorVariableName semantic surface color used by parent documentation sections.
 * @property dependencyVersionComponentId component used for dependency-version entries.
 * @property dependencyVersionInstanceNames allowed dependency-version instance names.
 * @property dependencyVersionProps semantic-to-Figma property mapping for version entries.
 * @property parentSectionSiblingGap spacing between top-level generated sections.
 * @property parentSectionNodeIds node ids of parent sections managed by the writer.
 * @property parentSectionCornerRadius corner radius shared by parent documentation sections.
 * @property sectionSiblingGap spacing between generated child sections.
 * @property treeNodeComponentIds tree node type to Figma component id mapping.
 * @property connectorTemplateName dependency-tree connector template name.
 * @property headerInstanceName canonical header instance layer name.
 * @property headerLinkPropertyName header property that receives repository links.
 * @property githubMainBlobUrl GitHub main-branch URL prefix for files.
 * @property githubMainTreeUrl GitHub main-branch URL prefix for directories.
 * @property ciConfigurationModelName selected CI configuration model.
 * @property ciPipelineName pipeline rendered as the primary CI flow.
 * @property figmaPipelineName pipeline rendered as the Figma publication flow.
 * @property teamCitySource canonical TeamCity configuration source link.
 * @property topologySource canonical CI topology source link.
 * @property windowsRuntimeSource canonical Windows runtime source link.
 * @property windowsRuntimeRunbookSource operational Windows runtime runbook link.
 * @property visualContractSource canonical visual contract link.
 * @property branchProtectionSource canonical branch-protection documentation link.
 * @property canonicalSyncSource canonical Figma sync workflow link.
 * @property canonicalDesignModelPath repository-relative canonical design model path.
 * @property repositoryRootRelativeToTools path from the writer tools to the repository root.
 * @property changeImpactPolicyRelativeToRepository repository-relative impact-policy path.
 * @property headerSectionTargets section headers managed by the writer.
 * @property versionSectionTargets version section configuration keyed by model name.
 * @property treeNodeProps semantic-to-Figma property mapping for dependency tree nodes.
 * @property artifactProps semantic-to-Figma property mapping for artifact nodes.
 * @property artifactsBundleProps semantic-to-Figma property mapping for artifact bundles.
 * @property artifactInstanceName canonical artifact instance layer name.
 * @property artifactsBundleInstanceName canonical artifact-bundle instance layer name.
 * @property usageChipComponentSetId component set used to render usage chips.
 * @property usageChipInstanceName canonical usage-chip instance name.
 * @property toolArtifactUsageInstanceName canonical tool-artifact usage instance name.
 * @property toolArtifactUsageProps semantic-to-Figma mapping for tool-artifact usages.
 * @property usageChipProps semantic-to-Figma property mapping for usage chips.
 * @property usageChipKinds usage kind to component variant mapping.
 * @property catalogTreeTargets dependency catalog tree targets managed by the writer.
 * @property ciVisualTargetNames CI visual targets managed by canonical publication.
 * @property defaultFixtureTargets fixture name to visual target mapping for local tests.
 */
data class FigmaWriterProjectConfig(
    val metadataPageId: String,
    val metadataNamespace: String,
    val figmaFileKey: String,
    val projectDisplayName: String,
    val mcpClientName: String,
    val ciDocumentationPageId: String,
    val ciNodeComponentId: String,
    val ciIconComponentSetId: String,
    val ciVariableCollectionName: String,
    val ciVariableModeNames: List<String>,
    val ciNodeInstanceName: String,
    val ciIconInstanceName: String,
    val ciIconEnvironmentProperty: String,
    val ciIconEnvironments: List<String>,
    val ciConnectorName: String,
    val ciConnectorTemplateName: String,
    val ciConnectorTemplateNodeId: String,
    val ciNodeProps: Map<String, String>,
    val ciNodePhaseContainerName: String,
    val ciNodeOutcomeContainerName: String,
    val ciPhaseComponentId: String,
    val ciPhaseSlotNamePrefix: String,
    val ciPhaseSlotCount: Int,
    val ciPhaseProps: Map<String, String>,
    val ciPhaseStepContainerName: String,
    val ciStepComponentSetId: String,
    val ciStepSlotNamePrefix: String,
    val ciStepSlotCount: Int,
    val ciStepRoles: List<String>,
    val ciStepProps: Map<String, String>,
    val ciOutcomeComponentSetId: String,
    val ciOutcomeSlotNamePrefix: String,
    val ciOutcomeSlotCount: Int,
    val ciOutcomeKinds: List<String>,
    val ciOutcomeProps: Map<String, String>,
    val versionsCollectionName: String,
    val versionAliasModeName: String,
    val versionNumberModeName: String,
    val outlineColorVariableName: String,
    val surfaceColorVariableName: String,
    val dependencyVersionComponentId: String,
    val dependencyVersionInstanceNames: List<String>,
    val dependencyVersionProps: Map<String, String>,
    val parentSectionSiblingGap: Int,
    val parentSectionNodeIds: List<String>,
    val parentSectionCornerRadius: Int,
    val sectionSiblingGap: Int,
    val treeNodeComponentIds: Map<String, String>,
    val connectorTemplateName: String,
    val headerInstanceName: String,
    val headerLinkPropertyName: String,
    val githubMainBlobUrl: String,
    val githubMainTreeUrl: String,
    val ciConfigurationModelName: String,
    val ciPipelineName: String,
    val figmaPipelineName: String,
    val teamCitySource: String,
    val topologySource: String,
    val windowsRuntimeSource: String,
    val windowsRuntimeRunbookSource: String,
    val visualContractSource: String,
    val branchProtectionSource: String,
    val canonicalSyncSource: String,
    val canonicalDesignModelPath: String,
    val repositoryRootRelativeToTools: String,
    val changeImpactPolicyRelativeToRepository: String,
    val headerSectionTargets: List<FigmaHeaderSectionTarget>,
    val versionSectionTargets: Map<String, FigmaVersionSectionTarget>,
    val treeNodeProps: Map<String, String>,
    val artifactProps: Map<String, String>,
    val artifactsBundleProps: Map<String, String>,
    val artifactInstanceName: String,
    val artifactsBundleInstanceName: String,
    val usageChipComponentSetId: String,
    val usageChipInstanceName: String,
    val toolArtifactUsageInstanceName: String,
    val toolArtifactUsageProps: Map<String, String>,
    val usageChipProps: Map<String, String>,
    val usageChipKinds: Map<String, String>,
    val catalogTreeTargets: List<FigmaCatalogTreeTargetConfig>,
    val ciVisualTargetNames: List<String>,
    val defaultFixtureTargets: Map<String, String>,
)
