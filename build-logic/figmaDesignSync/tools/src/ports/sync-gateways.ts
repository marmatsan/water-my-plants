import type { DesignModel } from "../domain/design-model";

export type VersionSyncResult = {
  updatedVersions: string[];
  createdVariables: string[];
  createdInstances: string[];
  mutatedNodeIds: string[];
};

export type CatalogTreeSyncResult = {
  updatedCatalogNodes: string[];
  createdCatalogNodes: string[];
  createdCatalogConnectors: string[];
  mutatedNodeIds: string[];
};

export type ModuleDependencySyncResult = {
  updatedModules: string[];
  hiddenModules: string[];
  updatedModuleConnectors: string[];
  removedModuleConnectors: string[];
  mutatedNodeIds: string[];
};

export type MetadataSyncResult = {
  metadata: {
    pageId: string;
    namespace: string;
    gitSha: string;
    modelHash: string;
  };
  mutatedNodeIds: string[];
};

export type VersionSyncGateway = {
  syncVersions(designModel: DesignModel): Promise<VersionSyncResult>;
};

export type CatalogTreeSyncGateway = {
  syncCatalogTrees(designModel: DesignModel): Promise<CatalogTreeSyncResult>;
};

export type ModuleDependencySyncGateway = {
  syncModuleDependencies(designModel: DesignModel): Promise<ModuleDependencySyncResult>;
};

export type MetadataSyncGateway = {
  writeMetadata(designModel: DesignModel): Promise<MetadataSyncResult>;
};
