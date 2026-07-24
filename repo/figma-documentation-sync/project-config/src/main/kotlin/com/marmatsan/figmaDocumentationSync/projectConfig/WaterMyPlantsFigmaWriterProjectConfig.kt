package com.marmatsan.figmaDocumentationSync.projectConfig

import com.marmatsan.figmaDocumentationSync.domain.model.writer.FigmaCatalogTreeTargetConfig
import com.marmatsan.figmaDocumentationSync.domain.model.writer.FigmaCatalogTreeTargetLifecycle
import com.marmatsan.figmaDocumentationSync.domain.model.writer.FigmaCatalogTreeTargetType
import com.marmatsan.figmaDocumentationSync.domain.model.writer.FigmaHeaderSectionTarget
import com.marmatsan.figmaDocumentationSync.domain.model.writer.FigmaProjectLink
import com.marmatsan.figmaDocumentationSync.domain.model.writer.FigmaVersionSectionTarget
import com.marmatsan.figmaDocumentationSync.domain.model.writer.FigmaWriterProjectConfig

internal object WaterMyPlantsFigmaWriterProjectConfig {
    private const val GITHUB_MAIN_BLOB_URL = "https://github.com/marmatsan/water-my-plants/blob/main"
    private const val GITHUB_MAIN_TREE_URL = "https://github.com/marmatsan/water-my-plants/tree/main"

    val value =
        FigmaWriterProjectConfig(
            metadataPageId = "62934:908",
            metadataNamespace = "water_my_plants_sync",
            figmaFileKey = "YBZXsd8oyGLbcI2KWxJvRK",
            projectDisplayName = "Water My Plants",
            mcpClientName = "water-my-plants-figma-sync",
            ciDocumentationPageId = "63153:2876",
            ciNodeComponentId = "64670:3088",
            ciIconComponentSetId = "64361:716",
            ciVariableCollectionName = "ci/cd",
            ciVariableModeNames =
                listOf(
                    "Actor",
                    "System",
                    "Git reference",
                    "Pipeline",
                    "Job",
                    "Artifact",
                    "Check",
                    "Gate",
                ),
            ciNodeInstanceName = ".ci node",
            ciIconInstanceName = ".ci icon",
            ciIconEnvironmentProperty = "environment",
            ciIconEnvironments =
                listOf(
                    "github",
                    "teamcity",
                    "cloudflare",
                    "figma",
                    "codex",
                    "browser",
                    "terminal",
                    "operator",
                    "json",
                    "gradle",
                ),
            ciConnectorName = ".ci connector",
            ciConnectorLabelName = ".ci connector label",
            ciConnectorTemplateSectionId = "63069:629",
            ciNodeProps =
                mapOf(
                    "name" to "name",
                    "description" to "description",
                    "executionPlanHeading" to "execution plan heading",
                    "source" to "source",
                    "runtimePlatform" to "runtime platform",
                    "runtimeService" to "runtime service",
                    "runtimeStartup" to "runtime startup",
                    "runtimeIdentity" to "runtime identity",
                    "showExecutionPlan" to "show execution plan",
                    "showOutcome" to "show outcome",
                    "showSource" to "show source",
                    "showRuntime" to "show runtime",
                    "showOptionalDetails" to "show optional details",
                ),
            ciNodePhaseContainerName = "execution plan",
            ciNodeOutcomeContainerName = "outcome",
            ciPhaseComponentId = "64668:2944",
            ciPhaseSlotNamePrefix = "phase",
            ciPhaseSlotCount = 8,
            ciPhaseProps =
                mapOf(
                    "order" to "order",
                    "title" to "title",
                    "technicalId" to "technical id",
                    "description" to "description",
                    "showTechnicalId" to "show technical id",
                    "showDescription" to "show description",
                    "showSteps" to "show steps",
                ),
            ciPhaseStepContainerName = "steps",
            ciStepComponentSetId = "64665:2991",
            ciStepSlotNamePrefix = "step",
            ciStepSlotCount = 8,
            ciStepRoles =
                listOf(
                    "action",
                    "decision",
                    "group",
                ),
            ciStepProps =
                mapOf(
                    "order" to "order",
                    "title" to "title",
                    "technicalId" to "technical id",
                    "tasks" to "tasks",
                    "description" to "description",
                    "condition" to "condition",
                    "showTechnicalId" to "show technical id",
                    "showDescription" to "show description",
                    "showCondition" to "show condition",
                    "role" to "role",
                ),
            ciOutcomeComponentSetId = "64669:3118",
            ciOutcomeSlotNamePrefix = "outcome",
            ciOutcomeSlotCount = 4,
            ciOutcomeKinds =
                listOf(
                    "artifact",
                    "check",
                    "success",
                    "action",
                ),
            ciOutcomeProps =
                mapOf(
                    "order" to "order",
                    "title" to "title",
                    "technicalId" to "technical id",
                    "description" to "description",
                    "condition" to "condition",
                    "showTechnicalId" to "show technical id",
                    "showDescription" to "show description",
                    "showCondition" to "show condition",
                    "kind" to "kind",
                ),
            versionsCollectionName = "repo\\dependency-catalog\\versions.properties",
            versionAliasModeName = "Version alias",
            versionNumberModeName = "Version number",
            outlineColorVariableName = "md/sys/color/outline",
            dependencyVersionComponentId = "63075:591",
            dependencyVersionInstanceNames =
                listOf(
                    ".dependency version",
                    ".project version",
                ),
            dependencyVersionProps =
                mapOf(
                    "alias" to "version alias#63075:0",
                    "number" to "version number#63075:1",
                ),
            parentSectionSiblingGap = 1139,
            parentSectionNodeIds =
                listOf(
                    "63685:108540",
                    "62936:183",
                    "63099:949",
                    "63099:954",
                    "63216:6907",
                    "63330:551",
                ),
            sectionSiblingGap = 114,
            treeNodeComponentIds =
                mapOf(
                    "Library" to "63069:681",
                    "Plugin" to "63069:694",
                ),
            connectorTemplateName = "simple-solid_arrow",
            headerInstanceName = ".Header",
            headerLinkPropertyName = "Link",
            githubMainBlobUrl = GITHUB_MAIN_BLOB_URL,
            githubMainTreeUrl = GITHUB_MAIN_TREE_URL,
            ciConfigurationModelName = "teamCity",
            ciPipelineName = "CI",
            figmaPipelineName = "Figma Sync",
            teamCitySource = ".teamcity/settings.kts",
            topologySource = "docs/ci/external-topology.yaml",
            windowsRuntimeSource = "docs/ci/windows-runtime.yaml",
            windowsRuntimeRunbookSource = "docs/runbooks/teamcity-cloudflare-access.md",
            visualContractSource = "docs/ci/visual-model-contract.md",
            branchProtectionSource = "docs/ci/main-branch-protection.md",
            canonicalSyncSource = "repo/figma-documentation-sync/docs/runbooks/canonical-artifact-visual-sync.md",
            canonicalDesignModelPath = "build/reports/figma-sync/design-model.json",
            repositoryRootRelativeToTools = "../../..",
            changeImpactPolicyRelativeToRepository =
                "repo/figma-documentation-sync/project-config/water-my-plants/change-impact-policy.json",
            headerSectionTargets =
                listOf(
                    FigmaHeaderSectionTarget(
                        sectionNodeId = "63685:108540",
                        links =
                            links(
                                "repo/dependency-catalog/catalog-core/src/main/kotlin/" +
                                    "com/marmatsan/dependencies/tree/dsl/library/LibraryTreeDsl.kt",
                                "repo/dependency-catalog/catalog-core/src/main/kotlin/" +
                                    "com/marmatsan/dependencies/tree/dsl/library/LibraryScope.kt",
                                "repo/dependency-catalog/catalog-core/src/main/kotlin/" +
                                    "com/marmatsan/dependencies/tree/dsl/plugin/PluginTreeDsl.kt",
                            ),
                    ),
                    FigmaHeaderSectionTarget(
                        sectionNodeId = "62936:183",
                        links =
                            links(
                                "repo/dependency-catalog/versions.properties",
                            ),
                    ),
                    FigmaHeaderSectionTarget(
                        sectionNodeId = "63099:949",
                        links =
                            links(
                                "repo/dependency-catalog/water-my-plants-catalog/src/main/kotlin/" +
                                    "com/marmatsan/dependencies/LibraryTrees.kt",
                                "repo/dependency-catalog/water-my-plants-catalog/src/main/kotlin/" +
                                    "com/marmatsan/dependencies/PluginTrees.kt",
                            ),
                    ),
                    FigmaHeaderSectionTarget(
                        sectionNodeId = "63099:954",
                        links =
                            links(
                                "repo/gradle-plugins/settings.gradle.kts",
                                "repo/figma-documentation-sync/settings.gradle.kts",
                            ),
                    ),
                    FigmaHeaderSectionTarget(
                        sectionNodeId = "63216:6907",
                        links =
                            listOf(
                                FigmaProjectLink(
                                    label = "repo/gradle-plugins",
                                    url = "$GITHUB_MAIN_TREE_URL/repo/gradle-plugins",
                                ),
                            ),
                    ),
                    FigmaHeaderSectionTarget(
                        sectionNodeId = "63330:551",
                        links =
                            links(
                                "repo/figma-documentation-sync/plugin/src/main/kotlin/com/marmatsan/" +
                                    "figmaDocumentationSync/" +
                                    "plugin/gradle/FigmaDocumentationSyncGradlePlugin.kt",
                            ),
                    ),
                ),
            versionSectionTargets =
                mapOf(
                    "Main project dependencies" to
                        FigmaVersionSectionTarget(
                            parentNodeId = "64247:3827",
                            variableFolder = "Main project dependencies",
                        ),
                    "Libraries" to
                        FigmaVersionSectionTarget(
                            parentNodeId = "64247:3853",
                            variableFolder = "Libraries",
                        ),
                    "Plugins" to
                        FigmaVersionSectionTarget(
                            parentNodeId = "64247:3854",
                            variableFolder = "Plugins",
                        ),
                ),
            treeNodeProps =
                mapOf(
                    "libraryGroup" to "Library group#1345:12",
                    "pluginId" to "Plugin ID#1345:16",
                    "showPluginVersion" to "Show plugin version#58719:0",
                    "showArtifacts" to "Show artifacts#63079:0",
                    "pluginVersion" to "Plugin version#63081:2",
                    "showAppliedByModule" to "Show applied by module",
                    "showUsedByConventionPlugin" to "Show used by convention plugin",
                    "showUnused" to "Show unused",
                    "showIsGradlePlugin" to "Show is a gradle plugin#63112:4",
                    "type" to "Type",
                ),
            artifactProps =
                mapOf(
                    "name" to "Artifact name",
                    "version" to "Artifact version",
                    "showVersion" to "Show version",
                    "showAppliedByPlugin" to "Show applied by plugin",
                    "showUsedByModule" to "Show used by module",
                    "showConfiguredAsTool" to "Show configured as tool",
                ),
            artifactsBundleProps =
                mapOf(
                    "alias" to "Alias",
                    "version" to "Version",
                    "showVersion" to "With version",
                    "showAppliedByPlugin" to "Show applied by plugin",
                    "showUsedByModule" to "Show used by module",
                ),
            artifactInstanceName = ".artifact",
            artifactsBundleInstanceName = ".artifacts bundle",
            usageChipComponentSetId = "63085:793",
            usageChipInstanceName = ".usage chip",
            toolArtifactUsageInstanceName = ".tool artifact usage",
            toolArtifactUsageProps = mapOf("target" to "tool artifact target"),
            usageChipProps =
                mapOf(
                    "kind" to "kind",
                    "name" to "name",
                ),
            usageChipKinds =
                mapOf(
                    "module" to "module",
                    "conventionPlugin" to "convention-plugin",
                ),
            catalogTreeTargets =
                listOf(
                    catalogTarget(
                        name = "waterMyPlants.libraries",
                        sectionNodeId = "63069:629",
                        type = FigmaCatalogTreeTargetType.LIBRARY,
                        lifecycle = FigmaCatalogTreeTargetLifecycle.STABLE_DOCUMENTATION_TARGET,
                        catalog = "waterMyPlants",
                        collection = "libraries",
                    ),
                    catalogTarget(
                        name = "waterMyPlants.plugins",
                        sectionNodeId = "63069:594",
                        type = FigmaCatalogTreeTargetType.PLUGIN,
                        lifecycle = FigmaCatalogTreeTargetLifecycle.STABLE_DOCUMENTATION_TARGET,
                        catalog = "waterMyPlants",
                        collection = "plugins",
                    ),
                    catalogTarget(
                        name = "waterMyPlants.customGradleConventionPlugins",
                        sectionNodeId = "63216:6907",
                        type = FigmaCatalogTreeTargetType.PLUGIN,
                        lifecycle = FigmaCatalogTreeTargetLifecycle.STABLE_DOCUMENTATION_TARGET,
                        catalog = "waterMyPlants",
                        collection = "customGradleConventionPlugins",
                        gradlePluginNodes = true,
                        warnWhenUnused = true,
                    ),
                    catalogTarget(
                        name = "waterMyPlants.customGradlePlugins",
                        sectionNodeId = "63330:551",
                        type = FigmaCatalogTreeTargetType.PLUGIN,
                        lifecycle = FigmaCatalogTreeTargetLifecycle.STABLE_DOCUMENTATION_TARGET,
                        catalog = "waterMyPlants",
                        collection = "customGradlePlugins",
                        gradlePluginNodes = true,
                        warnWhenUnused = true,
                    ),
                    catalogTarget(
                        name = "gradlePlugins.libraries",
                        sectionNodeId = "63099:951",
                        type = FigmaCatalogTreeTargetType.LIBRARY,
                        lifecycle = FigmaCatalogTreeTargetLifecycle.DECLARED_CATALOG_TARGET,
                        catalog = "gradlePlugins",
                        collection = "libraries",
                    ),
                    catalogTarget(
                        name = "gradlePlugins.plugins",
                        sectionNodeId = "63100:2952",
                        type = FigmaCatalogTreeTargetType.PLUGIN,
                        lifecycle = FigmaCatalogTreeTargetLifecycle.DECLARED_CATALOG_TARGET,
                        catalog = "gradlePlugins",
                        collection = "plugins",
                    ),
                    catalogTarget(
                        name = "figmaDocumentationSync.libraries",
                        sectionNodeId = "63573:260",
                        type = FigmaCatalogTreeTargetType.LIBRARY,
                        lifecycle = FigmaCatalogTreeTargetLifecycle.DECLARED_CATALOG_TARGET,
                        catalog = "figmaDocumentationSync",
                        collection = "libraries",
                    ),
                    catalogTarget(
                        name = "figmaDocumentationSync.plugins",
                        sectionNodeId = "63573:346",
                        type = FigmaCatalogTreeTargetType.PLUGIN,
                        lifecycle = FigmaCatalogTreeTargetLifecycle.DECLARED_CATALOG_TARGET,
                        catalog = "figmaDocumentationSync",
                        collection = "plugins",
                    ),
                ),
            ciVisualTargetNames =
                listOf(
                    "ci.overview",
                    "ci.pullRequestIntegration",
                    "ci.postMergeDesignDocumentation",
                    "ci.jobTasks",
                    "ci.infrastructureAndAccess",
                    "ci.windowsRuntime",
                ),
            defaultFixtureTargets =
                mapOf(
                    "catalog-tree" to "waterMyPlants.plugins",
                    "versions" to "versions",
                ),
        )

    private fun links(
        vararg paths: String,
    ): List<FigmaProjectLink> =
        paths.map { path ->
            FigmaProjectLink(
                label = path,
                url = "$GITHUB_MAIN_BLOB_URL/$path",
            )
        }

    private fun catalogTarget(
        name: String,
        sectionNodeId: String,
        type: FigmaCatalogTreeTargetType,
        lifecycle: FigmaCatalogTreeTargetLifecycle,
        catalog: String,
        collection: String,
        gradlePluginNodes: Boolean = false,
        warnWhenUnused: Boolean = false,
    ) = FigmaCatalogTreeTargetConfig(
        name = name,
        sectionNodeId = sectionNodeId,
        type = type,
        lifecycle = lifecycle,
        nodesPath =
            listOf(
                "content",
                "catalogs",
                catalog,
                collection,
            ),
        gradlePluginNodes = gradlePluginNodes,
        warnWhenUnused = warnWhenUnused,
    )
}
