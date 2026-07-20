package com.marmatsan.figmaDocumentationSync.data.json.writer

import com.marmatsan.figmaDocumentationSync.domain.model.writer.FigmaCatalogTreeTargetConfig
import com.marmatsan.figmaDocumentationSync.domain.model.writer.FigmaHeaderSectionTarget
import com.marmatsan.figmaDocumentationSync.domain.model.writer.FigmaVersionSectionTarget
import com.marmatsan.figmaDocumentationSync.domain.model.writer.FigmaWriterProjectConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Projects [FigmaWriterProjectConfig] to the language-neutral writer schema. */
object FigmaWriterProjectConfigJson {
    private val prettyJson = Json { prettyPrint = true }

    fun encode(
        config: FigmaWriterProjectConfig,
    ): String =
        prettyJson.encodeToString(
            JsonObject.serializer(),
            config.toJson(),
        ) + System.lineSeparator()

    private fun FigmaWriterProjectConfig.toJson(): JsonObject {
        val catalogTargetNames =
            catalogTreeTargets.map(
                transform = FigmaCatalogTreeTargetConfig::name,
            )
        return buildJsonObject {
            put(
                "schemaVersion",
                1,
            )
            put(
                "METADATA_PAGE_ID",
                metadataPageId,
            )
            put(
                "METADATA_NAMESPACE",
                metadataNamespace,
            )
            put(
                "FIGMA_FILE_KEY",
                figmaFileKey,
            )
            put(
                "PROJECT_DISPLAY_NAME",
                projectDisplayName,
            )
            put(
                "MCP_CLIENT_NAME",
                mcpClientName,
            )
            put(
                "OFFICIAL_STAGING_NAMESPACE",
                "${metadataNamespace}_staging",
            )
            put(
                "PREVIEW_STAGING_NAMESPACE",
                "${metadataNamespace}_preview",
            )
            put(
                "CI_DOCUMENTATION_PAGE_ID",
                ciDocumentationPageId,
            )
            put(
                "CI_NODE_COMPONENT_ID",
                ciNodeComponentId,
            )
            put(
                "CI_ICON_COMPONENT_SET_ID",
                ciIconComponentSetId,
            )
            put(
                "CI_VARIABLE_COLLECTION_NAME",
                ciVariableCollectionName,
            )
            put(
                "CI_VARIABLE_MODE_NAMES",
                ciVariableModeNames.toJsonArray(),
            )
            put(
                "CI_NODE_INSTANCE_NAME",
                ciNodeInstanceName,
            )
            put(
                "CI_ICON_INSTANCE_NAME",
                ciIconInstanceName,
            )
            put(
                "CI_ICON_ENVIRONMENT_PROPERTY",
                ciIconEnvironmentProperty,
            )
            put(
                "CI_ICON_ENVIRONMENTS",
                ciIconEnvironments.toJsonArray(),
            )
            put(
                "CI_CONNECTOR_NAME",
                ciConnectorName,
            )
            put(
                "CI_CONNECTOR_LABEL_NAME",
                ciConnectorLabelName,
            )
            put(
                "CI_CONNECTOR_TEMPLATE_SECTION_ID",
                ciConnectorTemplateSectionId,
            )
            put(
                "CI_NODE_PROPS",
                ciNodeProps.toJsonObject(),
            )
            put(
                "VERSIONS_COLLECTION_NAME",
                versionsCollectionName,
            )
            put(
                "VERSIONS_COLLECTION_NAMES",
                listOf(versionsCollectionName).toJsonArray(),
            )
            put(
                "VERSION_ALIAS_MODE_NAME",
                versionAliasModeName,
            )
            put(
                "VERSION_NUMBER_MODE_NAME",
                versionNumberModeName,
            )
            put(
                "OUTLINE_COLOR_VARIABLE_NAME",
                outlineColorVariableName,
            )
            put(
                "DEPENDENCY_VERSION_COMPONENT_ID",
                dependencyVersionComponentId,
            )
            put(
                "PROJECT_VERSION_COMPONENT_ID",
                dependencyVersionComponentId,
            )
            put(
                "DEPENDENCY_VERSION_INSTANCE_NAMES",
                dependencyVersionInstanceNames.toJsonArray(),
            )
            put(
                "DEPENDENCY_VERSION_PROPS",
                dependencyVersionProps.toJsonObject(),
            )
            put(
                "PARENT_SECTION_SIBLING_GAP",
                parentSectionSiblingGap,
            )
            put(
                "PARENT_SECTION_NODE_IDS",
                parentSectionNodeIds.toJsonArray(),
            )
            put(
                "SECTION_SIBLING_GAP",
                sectionSiblingGap,
            )
            put(
                "TREE_NODE_COMPONENT_IDS",
                treeNodeComponentIds.toJsonObject(),
            )
            put(
                "CONNECTOR_TEMPLATE_NAME",
                connectorTemplateName,
            )
            put(
                "HEADER_INSTANCE_NAME",
                headerInstanceName,
            )
            put(
                "HEADER_LINK_PROPERTY_NAME",
                headerLinkPropertyName,
            )
            put(
                "GITHUB_MAIN_BLOB_URL",
                githubMainBlobUrl,
            )
            put(
                "GITHUB_MAIN_TREE_URL",
                githubMainTreeUrl,
            )
            put(
                "CI_CONFIGURATION_MODEL_NAME",
                ciConfigurationModelName,
            )
            put(
                "CI_PIPELINE_NAME",
                ciPipelineName,
            )
            put(
                "FIGMA_PIPELINE_NAME",
                figmaPipelineName,
            )
            put(
                "TEAMCITY_SOURCE",
                teamCitySource,
            )
            put(
                "TOPOLOGY_SOURCE",
                topologySource,
            )
            put(
                "WINDOWS_RUNTIME_SOURCE",
                windowsRuntimeSource,
            )
            put(
                "WINDOWS_RUNTIME_RUNBOOK_SOURCE",
                windowsRuntimeRunbookSource,
            )
            put(
                "VISUAL_CONTRACT_SOURCE",
                visualContractSource,
            )
            put(
                "BRANCH_PROTECTION_SOURCE",
                branchProtectionSource,
            )
            put(
                "OFFICIAL_SYNC_SOURCE",
                officialSyncSource,
            )
            put(
                "OFFICIAL_DESIGN_MODEL_PATH",
                officialDesignModelPath,
            )
            put(
                "REPOSITORY_ROOT_RELATIVE_TO_TOOLS",
                repositoryRootRelativeToTools,
            )
            put(
                "CHANGE_IMPACT_POLICY_RELATIVE_TO_REPOSITORY",
                changeImpactPolicyRelativeToRepository,
            )
            put(
                "HEADER_SECTION_TARGETS",
                headerSectionTargets.toHeaderTargetsJson(),
            )
            put(
                "VERSION_SECTION_TARGETS",
                versionSectionTargets.toVersionTargetsJson(),
            )
            put(
                "TREE_NODE_PROPS",
                treeNodeProps.toJsonObject(),
            )
            put(
                "ARTIFACT_PROPS",
                artifactProps.toJsonObject(),
            )
            put(
                "ARTIFACTS_BUNDLE_PROPS",
                artifactsBundleProps.toJsonObject(),
            )
            put(
                "ARTIFACT_INSTANCE_NAME",
                artifactInstanceName,
            )
            put(
                "ARTIFACTS_BUNDLE_INSTANCE_NAME",
                artifactsBundleInstanceName,
            )
            put(
                "USAGE_CHIP_COMPONENT_SET_ID",
                usageChipComponentSetId,
            )
            put(
                "USAGE_CHIP_INSTANCE_NAME",
                usageChipInstanceName,
            )
            put(
                "TOOL_ARTIFACT_USAGE_INSTANCE_NAME",
                toolArtifactUsageInstanceName,
            )
            put(
                "TOOL_ARTIFACT_USAGE_PROPS",
                toolArtifactUsageProps.toJsonObject(),
            )
            put(
                "USAGE_CHIP_PROPS",
                usageChipProps.toJsonObject(),
            )
            put(
                "USAGE_CHIP_KINDS",
                usageChipKinds.toJsonObject(),
            )
            put(
                "CATALOG_TREE_TARGETS",
                catalogTreeTargets.toCatalogTargetsJson(),
            )
            put(
                "CI_VISUAL_TARGET_NAMES",
                ciVisualTargetNames.toJsonArray(),
            )
            put(
                "CATALOG_TARGET_NAMES",
                catalogTargetNames.toJsonArray(),
            )
            put(
                "WRITER_TARGET_NAMES",
                (
                    listOf(
                        "preflight",
                        "headers",
                        "versions",
                    ) +
                        catalogTargetNames + ciVisualTargetNames + "metadata"
                ).toJsonArray(),
            )
            put(
                "DEFAULT_FIXTURE_TARGETS",
                defaultFixtureTargets.toJsonObject(),
            )
        }
    }

    private fun List<FigmaHeaderSectionTarget>.toHeaderTargetsJson(): JsonArray =
        map { target ->
            buildJsonObject {
                put(
                    "sectionNodeId",
                    target.sectionNodeId,
                )
                put(
                    "links",
                    JsonArray(
                        target.links.map { link ->
                            buildJsonObject {
                                put(
                                    "label",
                                    link.label,
                                )
                                put(
                                    "url",
                                    link.url,
                                )
                            }
                        },
                    ),
                )
            }
        }.let(::JsonArray)

    private fun Map<String, FigmaVersionSectionTarget>.toVersionTargetsJson(): JsonObject =
        mapValues { (_, target) ->
            buildJsonObject {
                put(
                    "parentNodeId",
                    target.parentNodeId,
                )
                put(
                    "variableFolder",
                    target.variableFolder,
                )
            }
        }.let(::JsonObject)

    private fun List<FigmaCatalogTreeTargetConfig>.toCatalogTargetsJson(): JsonArray =
        map { target ->
            buildJsonObject {
                put(
                    "name",
                    target.name,
                )
                put(
                    "sectionNodeId",
                    target.sectionNodeId,
                )
                put(
                    "type",
                    target.type.wireValue,
                )
                put(
                    "lifecycle",
                    target.lifecycle.wireValue,
                )
                put(
                    "nodesPath",
                    target.nodesPath.toJsonArray(),
                )
                if (target.gradlePluginNodes) {
                    put(
                        "gradlePluginNodes",
                        true,
                    )
                }
                if (target.warnWhenUnused) {
                    put(
                        "warnWhenUnused",
                        true,
                    )
                }
            }
        }.let(::JsonArray)

    private fun Map<String, String>.toJsonObject(): JsonObject =
        mapValues { (_, value) -> JsonPrimitive(value) }.let(::JsonObject)

    private fun List<String>.toJsonArray(): JsonArray =
        map(
            transform = ::JsonPrimitive,
        ).let(::JsonArray)
}
