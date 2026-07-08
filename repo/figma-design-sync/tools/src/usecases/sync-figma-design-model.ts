import type { DesignModel, SyncFigmaDesignModelOptions, SyncTargetName } from "../domain/design-model";
import type {
  CatalogTreeSyncGateway,
  MetadataSyncGateway,
  VersionSyncGateway,
} from "../ports/sync-gateways";

export type SyncFigmaDesignModelDependencies = {
  versionSyncGateway: VersionSyncGateway;
  catalogTreeSyncGateway: CatalogTreeSyncGateway;
  metadataSyncGateway: MetadataSyncGateway;
};

export async function syncFigmaDesignModel(
  designModel: DesignModel | undefined,
  dependencies: SyncFigmaDesignModelDependencies,
  options: SyncFigmaDesignModelOptions = {}
) {
  if (!designModel) {
    throw new Error("Replace DESIGN_MODEL with build/reports/figma-sync/design-model.json.");
  }
  requireMainBranchDesignModel(designModel);

  const requestedTargets = resolveRequestedTargets(options);
  const completedTargets: SyncTargetName[] = [];
  const skippedTargets = ALL_SYNC_TARGETS.filter((target) => !requestedTargets.has(target));
  const versionSyncResult = requestedTargets.has("versions")
    ? await dependencies.versionSyncGateway.syncVersions(designModel)
    : emptyVersionSyncResult();
  if (requestedTargets.has("versions")) {
    completedTargets.push("versions");
  }

  const catalogTargets = CATALOG_SYNC_TARGETS.filter((target) => requestedTargets.has(target));
  const catalogSyncResult = catalogTargets.length > 0
    ? await dependencies.catalogTreeSyncGateway.syncCatalogTrees(
        designModel,
        {
          targetNames: catalogTargets,
          sectionNodeOverrides: options.sectionNodeOverrides,
        }
      )
    : emptyCatalogTreeSyncResult();
  completedTargets.push(...catalogTargets);

  const shouldWriteMetadata = requestedTargets.has("metadata") || options.writeMetadata === true;
  const metadataSyncResult = shouldWriteMetadata
    ? await dependencies.metadataSyncGateway.writeMetadata(designModel)
    : emptyMetadataSyncResult();
  if (shouldWriteMetadata) {
    completedTargets.push("metadata");
  }

  return {
    requestedTargets: [...requestedTargets],
    completedTargets,
    skippedTargets,
    updatedVersions: versionSyncResult.updatedVersions,
    createdVariables: versionSyncResult.createdVariables,
    createdInstances: versionSyncResult.createdInstances,
    updatedCatalogNodes: catalogSyncResult.updatedCatalogNodes,
    createdCatalogNodes: catalogSyncResult.createdCatalogNodes,
    createdCatalogConnectors: catalogSyncResult.createdCatalogConnectors,
    removedCatalogNodes: catalogSyncResult.removedCatalogNodes,
    removedCatalogConnectors: catalogSyncResult.removedCatalogConnectors,
    metadata: metadataSyncResult.metadata,
    mutatedNodeIds: [
      ...new Set([
        ...versionSyncResult.mutatedNodeIds,
        ...catalogSyncResult.mutatedNodeIds,
        ...metadataSyncResult.mutatedNodeIds,
      ]),
    ],
  };
}

function requireMainBranchDesignModel(designModel: DesignModel) {
  if (designModel.branch !== "main") {
    throw new Error(
      `Figma MCP sync only accepts the authoritative design-model.json generated from main. ` +
        `Found branch '${designModel.branch ?? "<missing>"}'. Use the artifact from ` +
        `TeamCity Figma Sync > Generate main design model.`
    );
  }
}

function resolveRequestedTargets(options: SyncFigmaDesignModelOptions) {
  const requestedTargets = new Set<SyncTargetName>(
    options.targets && options.targets.length > 0
      ? options.targets.map(canonicalTargetName)
      : ALL_SYNC_TARGETS
  );
  const unknownTargets = (options.targets || []).filter((target) =>
    !KNOWN_SYNC_TARGETS.includes(target)
  );

  if (unknownTargets.length > 0) {
    throw new Error(`Unknown Figma sync target(s): ${unknownTargets.join(", ")}.`);
  }

  if (options.writeMetadata === false) {
    requestedTargets.delete("metadata");
  }

  return requestedTargets;
}

function canonicalTargetName(target: SyncTargetName): SyncTargetName {
  return TARGET_ALIASES[target] || target;
}

function emptyVersionSyncResult() {
  return {
    updatedVersions: [],
    createdVariables: [],
    createdInstances: [],
    mutatedNodeIds: [],
  };
}

function emptyCatalogTreeSyncResult() {
  return {
    updatedCatalogNodes: [],
    createdCatalogNodes: [],
    createdCatalogConnectors: [],
    removedCatalogNodes: [],
    removedCatalogConnectors: [],
    mutatedNodeIds: [],
  };
}

function emptyMetadataSyncResult() {
  return {
    metadata: null,
    mutatedNodeIds: [],
  };
}

const CATALOG_SYNC_TARGETS: SyncTargetName[] = [
  "waterMyPlants.libraries",
  "waterMyPlants.plugins",
  "waterMyPlants.customGradleConventionPlugins",
  "waterMyPlants.customGradlePlugins",
  "gradlePlugins.libraries",
  "gradlePlugins.plugins",
  "figmaDesignSync.libraries",
  "figmaDesignSync.plugins",
];

const ALL_SYNC_TARGETS: SyncTargetName[] = [
  "versions",
  ...CATALOG_SYNC_TARGETS,
  "metadata",
];

const TARGET_ALIASES: Partial<Record<SyncTargetName, SyncTargetName>> = {};

const KNOWN_SYNC_TARGETS: SyncTargetName[] = [...ALL_SYNC_TARGETS];
