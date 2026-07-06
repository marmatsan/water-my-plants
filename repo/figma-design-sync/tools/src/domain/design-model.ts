export type DesignModel = Record<string, any>;

export type CatalogTreeType = "Library" | "Plugin";

export type CatalogTreeTarget = {
  name: string;
  aliases?: string[];
  sectionNodeId: string;
  type: CatalogTreeType;
  nodes: (designModel: DesignModel) => any[] | undefined;
  gradleConventionPluginNodes?: boolean;
};

export type FlattenedCatalogNode = Record<string, any> & {
  type: CatalogTreeType;
  label: string;
  parentPath: string[];
  path: string[];
  children: any[];
};

export type SyncIdList = string[];

export type SyncTargetName =
  | "versions"
  | "waterMyPlants.libraries"
  | "waterMyPlants.plugins"
  | "waterMyPlants.customGradleConventionPlugins"
  | "waterMyPlants.customGradlePlugins"
  | "gradlePlugins.libraries"
  | "gradlePlugins.plugins"
  | "metadata";

export type SyncFigmaDesignModelOptions = {
  targets?: SyncTargetName[];
  writeMetadata?: boolean;
};
