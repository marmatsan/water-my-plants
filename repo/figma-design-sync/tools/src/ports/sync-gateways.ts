import type { DesignModel } from "../domain/design-model";

export type VersionSyncResult = {
  updatedVersions: string[];
  createdVariables: string[];
  createdInstances: string[];
  mutatedNodeIds: string[];
};

export type HeaderSyncResult = {
  updatedHeaders: string[];
  mutatedNodeIds: string[];
};

export type CatalogTreeSyncResult = {
  updatedCatalogNodes: string[];
  createdCatalogNodes: string[];
  createdCatalogConnectors: string[];
  removedCatalogNodes: string[];
  removedCatalogConnectors: string[];
  mutatedNodeIds: string[];
};

export type CiDocumentationSyncResult = {
  updatedCiSections: string[];
  createdCiNodes: string[];
  createdCiConnectors: string[];
  mutatedNodeIds: string[];
};

export type CatalogTreeSyncOptions = {
  targetNames?: string[];
  sectionNodeOverrides?: Record<string, string>;
  rootFilters?: Record<string, string[]>;
  cleanupOnlyTargetNames?: string[];
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

export type VisualContractCheckOptions = {
  targetNames?: string[];
  sectionNodeOverrides?: Record<string, string>;
  rootFilters?: Record<string, string[]>;
};

export type VisualContractCheckResult = {
  checkedComponents: string[];
  checkedSections: string[];
  checkedVariables: string[];
  checkedTargets: string[];
  mutatedNodeIds: string[];
};

export type VersionSyncGateway = {
  syncVersions(designModel: DesignModel): Promise<VersionSyncResult>;
};

export type HeaderSyncGateway = {
  syncHeaders(): Promise<HeaderSyncResult>;
};

export type CatalogTreeSyncGateway = {
  syncCatalogTrees(designModel: DesignModel, options?: CatalogTreeSyncOptions): Promise<CatalogTreeSyncResult>;
};

export type CiDocumentationSyncGateway = {
  syncCiDocumentation(designModel: DesignModel, targetNames: string[]): Promise<CiDocumentationSyncResult>;
};

export type MetadataSyncGateway = {
  writeMetadata(designModel: DesignModel): Promise<MetadataSyncResult>;
};

export type VisualContractCheckGateway = {
  checkVisualContract(
    designModel: DesignModel,
    options?: VisualContractCheckOptions
  ): Promise<VisualContractCheckResult>;
};
