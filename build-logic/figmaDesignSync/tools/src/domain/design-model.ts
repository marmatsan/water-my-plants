export type DesignModel = Record<string, any>;

export type CatalogTreeType = "Library" | "Plugin";

export type CatalogTreeTarget = {
  name: string;
  sectionNodeId: string;
  type: CatalogTreeType;
  nodes: (designModel: DesignModel) => any[] | undefined;
};

export type ModuleDependencyTarget = {
  name: string;
  sectionNodeId: string;
  dependencies: (designModel: DesignModel) => ModuleDependency[] | undefined;
};

export type ModuleDependency = {
  dependentModule: string;
  dependencyModule: string;
};

export type FlattenedCatalogNode = Record<string, any> & {
  type: CatalogTreeType;
  label: string;
  parentPath: string[];
  path: string[];
  children: any[];
};

export type SyncIdList = string[];
