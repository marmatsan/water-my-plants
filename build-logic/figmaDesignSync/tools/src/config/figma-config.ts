import type { CatalogTreeTarget, ModuleDependencyTarget } from "../domain/design-model";

export const METADATA_PAGE_ID = "62934:908";
export const METADATA_NAMESPACE = "water_my_plants_sync";
export const VERSIONS_COLLECTION_NAME = "build-logic\\versions.properties";
export const VERSION_ALIAS_MODE_NAME = "Version alias";
export const VERSION_NUMBER_MODE_NAME = "Version number";
export const PROJECT_VERSION_COMPONENT_ID = "63075:591";
export const TREE_NODE_COMPONENT_IDS = {
  Library: "63069:681",
  Plugin: "63069:694",
};
export const CONNECTOR_TEMPLATE_NAME = "simple-solid_arrow";

export const VERSION_SECTION_TARGETS = {
  "Main project dependencies": {
    parentNodeId: "63075:634",
    variableFolder: "Main project dependencies",
  },
  Libraries: {
    parentNodeId: "63075:644",
    variableFolder: "Libraries",
  },
  Plugins: {
    parentNodeId: "63075:804",
    variableFolder: "Plugins",
  },
};

export const TREE_NODE_PROPS = {
  libraryGroup: "Library group#1345:12",
  pluginId: "Plugin ID#1345:16",
  showPluginVersion: "Show plugin version#58719:0",
  showArtifacts: "Show artifacts#63079:0",
  pluginVersion: "Plugin version#63081:2",
  showConsumerModule: "Show consumer module#63085:0",
  showIsGradleConventionPlugin: "Show is a gradle convention plugin#63112:4",
  type: "Type",
};

export const ARTIFACT_PROPS = {
  showConsumerModules: "Show consumer modules#63086:1",
};

export const ARTIFACTS_BUNDLE_PROPS = {
  showConsumerModules: "Show consumer modules#63107:0",
};

export const ARTIFACT_INSTANCE_NAME = ".artifact";
export const ARTIFACTS_BUNDLE_INSTANCE_NAME = ".artifacts bundle";
export const MODULE_INSTANCE_NAME = ".module";
export const MODULE_PROPS = {
  name: "name",
  size: "size",
};
export const SMALL_MODULE_SIZE = "small";
export const BIG_MODULE_SIZE = "big";

export const CATALOG_TREE_TARGETS: CatalogTreeTarget[] = [
  {
    name: "waterMyPlants.libraries",
    sectionNodeId: "63069:629",
    type: "Library",
    nodes: (designModel) => designModel.content?.catalogs?.waterMyPlants?.libraries,
  },
  {
    name: "waterMyPlants.plugins",
    sectionNodeId: "63069:594",
    type: "Plugin",
    nodes: (designModel) => designModel.content?.catalogs?.waterMyPlants?.plugins,
  },
  {
    name: "waterMyPlants.customGradleConventionPlugins",
    sectionNodeId: "63216:6907",
    type: "Plugin",
    nodes: (designModel) => designModel.content?.catalogs?.waterMyPlants?.customGradleConventionPlugins,
  },
  {
    name: "buildLogic.libraries",
    sectionNodeId: "63099:951",
    type: "Library",
    nodes: (designModel) => designModel.content?.catalogs?.buildLogic?.libraries,
  },
  {
    name: "buildLogic.plugins",
    sectionNodeId: "63100:2952",
    type: "Plugin",
    nodes: (designModel) => designModel.content?.catalogs?.buildLogic?.plugins,
  },
];

export const MODULE_DEPENDENCY_TARGETS: ModuleDependencyTarget[] = [
  {
    name: "waterMyPlants.moduleDependencies",
    sectionNodeId: "63112:2622",
    dependencies: (designModel) => designModel.content?.moduleDependencies?.main,
  },
  {
    name: "buildLogic.moduleDependencies",
    sectionNodeId: "63111:2516",
    dependencies: (designModel) => designModel.content?.moduleDependencies?.buildLogic,
  },
];
