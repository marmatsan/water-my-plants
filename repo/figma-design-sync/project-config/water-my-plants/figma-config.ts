import type { CatalogTreeTarget } from "../../tools/src/domain/design-model";

export const METADATA_PAGE_ID = "62934:908";
export const METADATA_NAMESPACE = "water_my_plants_sync";
export const FIGMA_FILE_KEY = "YBZXsd8oyGLbcI2KWxJvRK";
export const PROJECT_DISPLAY_NAME = "Water My Plants";
export const MCP_CLIENT_NAME = "water-my-plants-figma-sync";
export const OFFICIAL_STAGING_NAMESPACE = `${METADATA_NAMESPACE}_staging`;
export const PREVIEW_STAGING_NAMESPACE = `${METADATA_NAMESPACE}_preview`;
export const CI_DOCUMENTATION_PAGE_ID = "63153:2876";
export const CI_NODE_COMPONENT_ID = "64301:3927";
export const CI_ICON_COMPONENT_SET_ID = "64361:716";
export const CI_VARIABLE_COLLECTION_NAME = "ci/cd";
export const CI_VARIABLE_MODE_NAMES = [
  "Actor",
  "System",
  "Git reference",
  "Pipeline",
  "Job",
  "Artifact",
  "Check",
  "Gate",
] as const;
export const CI_NODE_INSTANCE_NAME = ".ci node";
export const CI_ICON_INSTANCE_NAME = ".ci icon";
export const CI_ICON_ENVIRONMENT_PROPERTY = "environment";
export const CI_ICON_ENVIRONMENTS = [
  "github",
  "teamcity",
  "cloudflare",
  "figma",
  "codex",
  "browser",
  "terminal",
  "operator",
  "json",
] as const;
export const CI_CONNECTOR_NAME = ".ci connector";
export const CI_CONNECTOR_LABEL_NAME = ".ci connector label";
export const CI_CONNECTOR_TEMPLATE_SECTION_ID = "63069:629";
export const CI_NODE_PROPS = {
  name: "name",
  description: "description",
  steps: "steps",
  source: "source",
  runtimePlatform: "runtime platform",
  runtimeService: "runtime service",
  runtimeStartup: "runtime startup",
  runtimeIdentity: "runtime identity",
  showSteps: "show steps",
  showSource: "show source",
  showRuntime: "show runtime",
};
export const VERSIONS_COLLECTION_NAME = "repo\\dependency-catalog\\versions.properties";
export const VERSIONS_COLLECTION_NAMES = [VERSIONS_COLLECTION_NAME];
export const VERSION_ALIAS_MODE_NAME = "Version alias";
export const VERSION_NUMBER_MODE_NAME = "Version number";
export const OUTLINE_COLOR_VARIABLE_NAME = "md/sys/color/outline";
export const DEPENDENCY_VERSION_COMPONENT_ID = "63075:591";
export const PROJECT_VERSION_COMPONENT_ID = DEPENDENCY_VERSION_COMPONENT_ID;
export const DEPENDENCY_VERSION_INSTANCE_NAMES = [".dependency version", ".project version"];
export const DEPENDENCY_VERSION_PROPS = {
  alias: "version alias#63075:0",
  number: "version number#63075:1",
};
export const PARENT_SECTION_SIBLING_GAP = 1139;
export const PARENT_SECTION_NODE_IDS = [
  "63685:108540",
  "62936:183",
  "63099:949",
  "63099:954",
  "63216:6907",
  "63330:551",
];
export const SECTION_SIBLING_GAP = 114;
export const TREE_NODE_COMPONENT_IDS = {
  Library: "63069:681",
  Plugin: "63069:694",
};
export const CONNECTOR_TEMPLATE_NAME = "simple-solid_arrow";
export const HEADER_INSTANCE_NAME = ".Header";
export const HEADER_LINK_PROPERTY_NAME = "Link";

export const GITHUB_MAIN_BLOB_URL = "https://github.com/marmatsan/water-my-plants/blob/main";
export const GITHUB_MAIN_TREE_URL = "https://github.com/marmatsan/water-my-plants/tree/main";
export const CI_PIPELINE_NAME = "CI";
export const FIGMA_PIPELINE_NAME = "Figma Sync";
export const TEAMCITY_SOURCE = ".teamcity/settings.kts";
export const TOPOLOGY_SOURCE = "docs/ci/external-topology.yaml";
export const WINDOWS_RUNTIME_SOURCE = "docs/ci/windows-runtime.yaml";
export const WINDOWS_RUNTIME_RUNBOOK_SOURCE = "docs/runbooks/teamcity-cloudflare-access.md";
export const VISUAL_CONTRACT_SOURCE = "docs/ci/visual-model-contract.md";
export const BRANCH_PROTECTION_SOURCE = "docs/ci/main-branch-protection.md";
export const OFFICIAL_SYNC_SOURCE =
  "repo/figma-design-sync/docs/runbooks/official-artifact-visual-sync.md";
export const OFFICIAL_DESIGN_MODEL_PATH = "build/reports/figma-sync/design-model.json";
export const CHANGE_IMPACT_POLICY_RELATIVE_TO_MODULE =
  "project-config/water-my-plants/change-impact-policy.json";

export const HEADER_SECTION_TARGETS = [
  {
    sectionNodeId: "63685:108540",
    links: [
      "repo/dependency-catalog/catalog-core/src/main/kotlin/com/marmatsan/dependencies/tree/dsl/library/LibraryTreeDsl.kt",
      "repo/dependency-catalog/catalog-core/src/main/kotlin/com/marmatsan/dependencies/tree/dsl/library/LibraryScope.kt",
      "repo/dependency-catalog/catalog-core/src/main/kotlin/com/marmatsan/dependencies/tree/dsl/plugin/PluginTreeDsl.kt",
    ].map((path) => ({ label: path, url: `${GITHUB_MAIN_BLOB_URL}/${path}` })),
  },
  {
    sectionNodeId: "62936:183",
    links: [
      "repo/dependency-catalog/versions.properties",
    ].map((path) => ({ label: path, url: `${GITHUB_MAIN_BLOB_URL}/${path}` })),
  },
  {
    sectionNodeId: "63099:949",
    links: [
      "repo/dependency-catalog/water-my-plants-catalog/src/main/kotlin/com/marmatsan/dependencies/LibraryTrees.kt",
      "repo/dependency-catalog/water-my-plants-catalog/src/main/kotlin/com/marmatsan/dependencies/PluginTrees.kt",
    ].map((path) => ({ label: path, url: `${GITHUB_MAIN_BLOB_URL}/${path}` })),
  },
  {
    sectionNodeId: "63099:954",
    links: [
      "repo/gradle-plugins/settings.gradle.kts",
      "repo/figma-design-sync/settings.gradle.kts",
    ].map((path) => ({ label: path, url: `${GITHUB_MAIN_BLOB_URL}/${path}` })),
  },
  {
    sectionNodeId: "63216:6907",
    links: [{
      label: "repo/gradle-plugins",
      url: `${GITHUB_MAIN_TREE_URL}/repo/gradle-plugins`,
    }],
  },
  {
    sectionNodeId: "63330:551",
    links: [
      "repo/figma-design-sync/plugin/src/main/kotlin/com/marmatsan/figmaDesignSync/plugin/gradle/FigmaDesignSyncGradlePlugin.kt",
    ].map((path) => ({ label: path, url: `${GITHUB_MAIN_BLOB_URL}/${path}` })),
  },
];

export const VERSION_SECTION_TARGETS = {
  "Main project dependencies": {
    parentNodeId: "64247:3827",
    variableFolder: "Main project dependencies",
  },
  Libraries: {
    parentNodeId: "64247:3853",
    variableFolder: "Libraries",
  },
  Plugins: {
    parentNodeId: "64247:3854",
    variableFolder: "Plugins",
  },
};

export const TREE_NODE_PROPS = {
  libraryGroup: "Library group#1345:12",
  pluginId: "Plugin ID#1345:16",
  showPluginVersion: "Show plugin version#58719:0",
  showArtifacts: "Show artifacts#63079:0",
  pluginVersion: "Plugin version#63081:2",
  showAppliedByModule: "Show applied by module",
  showUsedByConventionPlugin: "Show used by convention plugin",
  showUnused: "Show unused",
  showIsGradlePlugin: "Show is a gradle plugin#63112:4",
  type: "Type",
};

export const ARTIFACT_PROPS = {
  name: "Artifact name",
  version: "Artifact version",
  showVersion: "Show version",
  showAppliedByPlugin: "Show applied by plugin",
  showUsedByModule: "Show used by module",
  showConfiguredAsTool: "Show configured as tool",
};

export const ARTIFACTS_BUNDLE_PROPS = {
  alias: "Alias",
  version: "Version",
  showVersion: "With version",
  showAppliedByPlugin: "Show applied by plugin",
  showUsedByModule: "Show used by module",
};

export const ARTIFACT_INSTANCE_NAME = ".artifact";
export const ARTIFACTS_BUNDLE_INSTANCE_NAME = ".artifacts bundle";
export const USAGE_CHIP_COMPONENT_SET_ID = "63085:793";
export const USAGE_CHIP_INSTANCE_NAME = ".usage chip";
export const TOOL_ARTIFACT_USAGE_INSTANCE_NAME = ".tool artifact usage";
export const TOOL_ARTIFACT_USAGE_PROPS = {
  target: "tool artifact target",
};
export const USAGE_CHIP_PROPS = {
  kind: "kind",
  name: "name",
};
export const USAGE_CHIP_KINDS = {
  module: "module",
  conventionPlugin: "convention-plugin",
};

export const CATALOG_TREE_TARGETS: CatalogTreeTarget[] = [
  {
    name: "waterMyPlants.libraries",
    sectionNodeId: "63069:629",
    type: "Library",
    lifecycle: "stableDocumentationTarget",
    nodes: (designModel) => designModel.content?.catalogs?.waterMyPlants?.libraries,
  },
  {
    name: "waterMyPlants.plugins",
    sectionNodeId: "63069:594",
    type: "Plugin",
    lifecycle: "stableDocumentationTarget",
    nodes: (designModel) => designModel.content?.catalogs?.waterMyPlants?.plugins,
  },
  {
    name: "waterMyPlants.customGradleConventionPlugins",
    sectionNodeId: "63216:6907",
    type: "Plugin",
    lifecycle: "stableDocumentationTarget",
    gradlePluginNodes: true,
    warnWhenUnused: true,
    nodes: (designModel) => designModel.content?.catalogs?.waterMyPlants?.customGradleConventionPlugins,
  },
  {
    name: "waterMyPlants.customGradlePlugins",
    sectionNodeId: "63330:551",
    type: "Plugin",
    lifecycle: "stableDocumentationTarget",
    gradlePluginNodes: true,
    warnWhenUnused: true,
    nodes: (designModel) => designModel.content?.catalogs?.waterMyPlants?.customGradlePlugins,
  },
  {
    name: "gradlePlugins.libraries",
    sectionNodeId: "63099:951",
    type: "Library",
    lifecycle: "declaredCatalogTarget",
    nodes: (designModel) => designModel.content?.catalogs?.gradlePlugins?.libraries,
  },
  {
    name: "gradlePlugins.plugins",
    sectionNodeId: "63100:2952",
    type: "Plugin",
    lifecycle: "declaredCatalogTarget",
    nodes: (designModel) => designModel.content?.catalogs?.gradlePlugins?.plugins,
  },
  {
    name: "figmaDesignSync.libraries",
    sectionNodeId: "63573:260",
    type: "Library",
    lifecycle: "declaredCatalogTarget",
    nodes: (designModel) => designModel.content?.catalogs?.figmaDesignSync?.libraries,
  },
  {
    name: "figmaDesignSync.plugins",
    sectionNodeId: "63573:346",
    type: "Plugin",
    lifecycle: "declaredCatalogTarget",
    nodes: (designModel) => designModel.content?.catalogs?.figmaDesignSync?.plugins,
  },
];

export const CI_VISUAL_TARGET_NAMES = [
  "ci.overview",
  "ci.pullRequestIntegration",
  "ci.postMergeDesignDocumentation",
  "ci.infrastructureAndAccess",
  "ci.windowsRuntime",
] as const;

export const CATALOG_TARGET_NAMES = CATALOG_TREE_TARGETS.map((target) => target.name);

export const WRITER_TARGET_NAMES = [
  "preflight",
  "headers",
  "versions",
  ...CATALOG_TARGET_NAMES,
  ...CI_VISUAL_TARGET_NAMES,
  "metadata",
];

export const DEFAULT_FIXTURE_TARGETS = {
  "catalog-tree": "waterMyPlants.plugins",
  versions: "versions",
};
