export type DesignModel = Record<string, any>;

export type CatalogTreeType = "Library" | "Plugin";
export type CatalogTreeTargetLifecycle = "stableDocumentationTarget" | "declaredCatalogTarget";

export type CatalogTreeTarget = {
  name: string;
  aliases?: string[];
  sectionNodeId: string;
  type: CatalogTreeType;
  lifecycle: CatalogTreeTargetLifecycle;
  nodes: (designModel: DesignModel) => any[] | undefined;
  gradlePluginNodes?: boolean;
  warnWhenUnused?: boolean;
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
  | "preflight"
  | "headers"
  | "versions"
  | "metadata"
  | (string & {});

export type SyncFigmaDesignModelOptions = {
  targets?: SyncTargetName[];
  writeMetadata?: boolean;
  sectionNodeOverrides?: Record<string, string>;
  catalogRootFilters?: Partial<Record<SyncTargetName, string[]>>;
  catalogCleanupOnlyTargets?: SyncTargetName[];
  executionMetadata?: SyncExecutionMetadata;
};

export type SyncExecutionMetadata = {
  writerHash: string;
  transportHash: string;
  targetFingerprints: Record<string, string>;
  writerScopeFingerprints: Record<string, string>;
  writerScopeFingerprintSchemaVersion: number;
};
