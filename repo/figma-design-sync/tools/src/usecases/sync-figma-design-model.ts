import type { DesignModel, SyncFigmaDesignModelOptions, SyncTargetName } from "../domain/design-model";
import {
  CATALOG_TREE_TARGETS,
  CI_VISUAL_TARGET_NAMES,
} from "@figma-design-sync/project-config";
import type {
  CatalogTreeSyncGateway,
  CiDocumentationSyncGateway,
  HeaderSyncGateway,
  MetadataSyncGateway,
  VersionSyncGateway,
  VisualContractCheckGateway,
} from "../ports/sync-gateways";

export type SyncFigmaDesignModelDependencies = {
  versionSyncGateway: VersionSyncGateway;
  headerSyncGateway: HeaderSyncGateway;
  catalogTreeSyncGateway: CatalogTreeSyncGateway;
  ciDocumentationSyncGateway: CiDocumentationSyncGateway;
  metadataSyncGateway: MetadataSyncGateway;
  visualContractCheckGateway: VisualContractCheckGateway;
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
  const shouldWriteMetadata = requestedTargets.has("metadata") || options.writeMetadata === true;
  if (shouldWriteMetadata && requestedTargets.size > 1) {
    throw new Error("Figma sync metadata must run alone after the complete visual sync.");
  }
  if (shouldWriteMetadata && !options.executionMetadata) {
    throw new Error(
      "Figma sync metadata requires writerHash, transportHash, model target fingerprints, " +
        "and writer scope fingerprints."
    );
  }
  const completedTargets: SyncTargetName[] = [];
  const skippedTargets = ALL_SYNC_TARGETS.filter((target) => !requestedTargets.has(target));
  const visualTargets = [...requestedTargets].filter(
    (target) => target !== "preflight" && target !== "metadata"
  );
  const preflightResult = requestedTargets.has("preflight")
    ? await dependencies.visualContractCheckGateway.checkVisualContract(
        designModel,
        {
          targetNames: visualTargets.length > 0 ? visualTargets : undefined,
          sectionNodeOverrides: options.sectionNodeOverrides,
          rootFilters: options.catalogRootFilters,
        }
      )
    : emptyVisualContractCheckResult();
  if (requestedTargets.has("preflight")) {
    completedTargets.push("preflight");
  }

  const headerSyncResult = requestedTargets.has("headers")
    ? await dependencies.headerSyncGateway.syncHeaders()
    : emptyHeaderSyncResult();
  if (requestedTargets.has("headers")) {
    completedTargets.push("headers");
  }

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
          rootFilters: options.catalogRootFilters,
          cleanupOnlyTargetNames: options.catalogCleanupOnlyTargets,
        }
      )
    : emptyCatalogTreeSyncResult();
  completedTargets.push(...catalogTargets);

  const ciTargets = CI_SYNC_TARGETS.filter((target) => requestedTargets.has(target));
  const ciSyncResult = ciTargets.length > 0
    ? await dependencies.ciDocumentationSyncGateway.syncCiDocumentation(designModel, ciTargets)
    : emptyCiDocumentationSyncResult();
  completedTargets.push(...ciTargets);

  const metadataSyncResult = shouldWriteMetadata
    ? await dependencies.metadataSyncGateway.writeMetadata(designModel, options.executionMetadata)
    : emptyMetadataSyncResult();
  if (shouldWriteMetadata) {
    completedTargets.push("metadata");
  }

  return {
    requestedTargets: [...requestedTargets],
    completedTargets,
    skippedTargets,
    updatedHeaders: headerSyncResult.updatedHeaders,
    updatedVersions: versionSyncResult.updatedVersions,
    createdVariables: versionSyncResult.createdVariables,
    createdInstances: versionSyncResult.createdInstances,
    updatedCatalogNodes: catalogSyncResult.updatedCatalogNodes,
    createdCatalogNodes: catalogSyncResult.createdCatalogNodes,
    createdCatalogConnectors: catalogSyncResult.createdCatalogConnectors,
    removedCatalogNodes: catalogSyncResult.removedCatalogNodes,
    removedCatalogConnectors: catalogSyncResult.removedCatalogConnectors,
    updatedCiSections: ciSyncResult.updatedCiSections,
    createdCiNodes: ciSyncResult.createdCiNodes,
    createdCiConnectors: ciSyncResult.createdCiConnectors,
    checkedComponents: preflightResult.checkedComponents,
    checkedSections: preflightResult.checkedSections,
    checkedVariables: preflightResult.checkedVariables,
    checkedTargets: preflightResult.checkedTargets,
    metadata: metadataSyncResult.metadata,
    mutatedNodeIds: [
      ...new Set([
        ...preflightResult.mutatedNodeIds,
        ...headerSyncResult.mutatedNodeIds,
        ...versionSyncResult.mutatedNodeIds,
        ...catalogSyncResult.mutatedNodeIds,
        ...ciSyncResult.mutatedNodeIds,
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
      : FULL_VISUAL_SYNC_TARGETS
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

function emptyHeaderSyncResult() {
  return {
    updatedHeaders: [],
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

function emptyCiDocumentationSyncResult() {
  return {
    updatedCiSections: [],
    createdCiNodes: [],
    createdCiConnectors: [],
    mutatedNodeIds: [],
  };
}

function emptyVisualContractCheckResult() {
  return {
    checkedComponents: [],
    checkedSections: [],
    checkedVariables: [],
    checkedTargets: [],
    mutatedNodeIds: [],
  };
}

const CATALOG_SYNC_TARGETS: SyncTargetName[] = CATALOG_TREE_TARGETS.map((target) => target.name);

const CI_SYNC_TARGETS: SyncTargetName[] = [...CI_VISUAL_TARGET_NAMES];

const VISUAL_SYNC_TARGETS: SyncTargetName[] = [
  "headers",
  "versions",
  ...CATALOG_SYNC_TARGETS,
  ...CI_SYNC_TARGETS,
];

const FULL_VISUAL_SYNC_TARGETS: SyncTargetName[] = [
  "preflight",
  ...VISUAL_SYNC_TARGETS,
];

const ALL_SYNC_TARGETS: SyncTargetName[] = [
  ...FULL_VISUAL_SYNC_TARGETS,
  "metadata",
];

const TARGET_ALIASES: Partial<Record<SyncTargetName, SyncTargetName>> = {};

const KNOWN_SYNC_TARGETS: SyncTargetName[] = [
  ...ALL_SYNC_TARGETS,
];
