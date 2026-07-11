import {
  DEPENDENCY_VERSION_COMPONENT_ID,
  DEPENDENCY_VERSION_INSTANCE_NAMES,
  DEPENDENCY_VERSION_PROPS,
  VERSION_ALIAS_MODE_NAME,
  VERSION_NUMBER_MODE_NAME,
  VERSION_SECTION_TARGETS,
  VERSIONS_COLLECTION_NAMES,
} from "../config/figma-config";
import type { DesignModel } from "../domain/design-model";
import type { VersionSyncGateway } from "../ports/sync-gateways";
import {
  loadVariablesByVersionKey,
  requireComponent,
  requireFrame,
  requireModeId,
  requireVariableCollection,
  resizeAncestorSectionsToFit,
  resizeNodeToFit,
  stackAncestorSectionSiblingsWithGap,
  getComponentPropertyValue,
} from "./figma-node-gateway";
import { collectText } from "./figma-text-gateway";

const DEPENDENCY_VERSION_MAX_COLUMNS = 2;
const DEPENDENCY_VERSION_COLUMN_GAP = 64;
const DEPENDENCY_VERSION_ROW_GAP = 32;
const VERSION_SECTION_GAP = 128;

export class FigmaVersionSyncGateway implements VersionSyncGateway {
  async syncVersions(designModel: DesignModel) {
    const versionSections = requireVersionSections(designModel);
    const collection = await requireVariableCollection(VERSIONS_COLLECTION_NAMES);
    const versionAliasModeId = requireModeId(collection, VERSION_ALIAS_MODE_NAME);
    const versionNumberModeId = requireModeId(collection, VERSION_NUMBER_MODE_NAME);
    const dependencyVersionComponent = await requireComponent(DEPENDENCY_VERSION_COMPONENT_ID);

    const variables = await loadVariablesByVersionKey(collection);
    const mutatedNodeIds = [];
    const createdVariables = [];
    const createdInstances = [];
    const updatedVersions = [];
    const syncedParents = [];

    for (const section of versionSections) {
      const target = VERSION_SECTION_TARGETS[section.name];
      if (!target) {
        throw new Error(`No Figma target configured for version section '${section.name}'.`);
      }

      const parent = await requireFrame(target.parentNodeId);
      const entries = Object.entries(section.versions);
      const orderedVersionKeys = entries.map(([versionKey]) => versionKey);
      const existingInstances = findDependencyVersionInstances(parent);
      const instancePlan = planDependencyVersionInstanceSync(
        existingInstances,
        orderedVersionKeys,
        readDependencyVersionKey
      );

      for (const instance of [...instancePlan.staleInstances, ...instancePlan.duplicateInstances]) {
        mutatedNodeIds.push(instance.id);
        instance.remove();
      }

      for (const [versionKey, versionNumber] of entries) {
        let variable = variables.get(versionKey);

        if (!variable) {
          variable = figma.variables.createVariable(
            `${target.variableFolder}/${versionKey}`,
            collection,
            "STRING"
          );
          variable.scopes = ["TEXT_CONTENT"];
          variables.set(versionKey, variable);
          createdVariables.push(variable.name);
        }

        variable.setValueForMode(versionAliasModeId, versionKey);
        variable.setValueForMode(versionNumberModeId, versionNumber);
        updatedVersions.push(versionKey);

        let instance = instancePlan.existingInstancesByVersionKey.get(versionKey);
        if (!instance) {
          instance = dependencyVersionComponent.createInstance();

          parent.appendChild(instance);

          mutatedNodeIds.push(instance.id);
          createdInstances.push(versionKey);
        }

        bindDependencyVersionInstance(instance, variable);
        mutatedNodeIds.push(variable.id);
      }

      layoutDependencyVersionGrid(parent, orderedVersionKeys, readDependencyVersionKey, mutatedNodeIds);
      resizeNodeToFit(parent, parent.children.filter((child) => child.visible !== false), mutatedNodeIds);
      syncedParents.push(parent);
    }

    stackVersionSectionFrames(syncedParents, mutatedNodeIds);
    for (const parent of syncedParents) {
      resizeNodeToFit(parent, parent.children.filter((child) => child.visible !== false), mutatedNodeIds);
    }
    if (syncedParents[0]) {
      stackAncestorSectionSiblingsWithGap(syncedParents[0], mutatedNodeIds);
      resizeAncestorSectionsToFit(syncedParents[0], mutatedNodeIds);
    }

    return {
      updatedVersions,
      createdVariables,
      createdInstances,
      mutatedNodeIds,
    };
  }
}

function requireVersionSections(designModel: DesignModel) {
  const sections = designModel.content?.versionSections;
  if (!Array.isArray(sections) || sections.length === 0) {
    throw new Error("designModel.content.versionSections is required for visual version sync.");
  }
  return sections;
}

export function planDependencyVersionInstanceSync(instances, expectedVersionKeys, readVersionKey) {
  const expectedVersionKeySet = new Set(expectedVersionKeys);
  const existingInstancesByVersionKey = new Map();
  const staleInstances = [];
  const duplicateInstances = [];

  for (const instance of instances) {
    const versionKey = readVersionKey(instance);
    if (!versionKey || !expectedVersionKeySet.has(versionKey)) {
      staleInstances.push(instance);
      continue;
    }

    if (existingInstancesByVersionKey.has(versionKey)) {
      duplicateInstances.push(instance);
      continue;
    }

    existingInstancesByVersionKey.set(versionKey, instance);
  }

  return {
    existingInstancesByVersionKey,
    staleInstances,
    duplicateInstances,
  };
}

function findDependencyVersionInstances(parent) {
  return parent.children.filter((child) =>
    child.type === "INSTANCE" && DEPENDENCY_VERSION_INSTANCE_NAMES.includes(child.name)
  );
}

function readDependencyVersionKey(instance) {
  const aliasPropertyValue = getComponentPropertyValue(instance, DEPENDENCY_VERSION_PROPS.alias);
  if (typeof aliasPropertyValue === "string" && aliasPropertyValue.trim()) {
    return aliasPropertyValue.trim();
  }

  return collectText(instance)
    .map((value) => value.trim())
    .find(Boolean);
}

export function dependencyVersionGridPosition(index: number, itemWidth: number, rowHeights: number[]): { x: number; y: number } {
  const column = index % DEPENDENCY_VERSION_MAX_COLUMNS;
  const row = Math.floor(index / DEPENDENCY_VERSION_MAX_COLUMNS);
  const y = rowHeights
    .slice(0, row)
    .reduce((sum, height) => sum + height + DEPENDENCY_VERSION_ROW_GAP, 0);

  return {
    x: column * (itemWidth + DEPENDENCY_VERSION_COLUMN_GAP),
    y,
  };
}

function layoutDependencyVersionGrid(parent, orderedVersionKeys, readVersionKey, mutatedNodeIds) {
  const instancesByVersionKey = new Map(
    findDependencyVersionInstances(parent)
      .map((instance) => [readVersionKey(instance), instance])
      .filter(([versionKey]) => Boolean(versionKey))
  );
  const orderedInstances = orderedVersionKeys
    .map((versionKey) => instancesByVersionKey.get(versionKey))
    .filter(Boolean);

  if (orderedInstances.length === 0) return;

  const columnWidth = Math.max(...orderedInstances.map((instance) => instance.width));
  const rowHeights = [];
  for (let index = 0; index < orderedInstances.length; index += DEPENDENCY_VERSION_MAX_COLUMNS) {
    rowHeights.push(
      Math.max(...orderedInstances.slice(index, index + DEPENDENCY_VERSION_MAX_COLUMNS).map((instance) => instance.height))
    );
  }

  orderedInstances.forEach((instance, index) => {
    const position = dependencyVersionGridPosition(index, columnWidth, rowHeights);
    if (Math.abs(instance.x - position.x) > 0.01) {
      instance.x = position.x;
      mutatedNodeIds.push(instance.id);
    }
    if (Math.abs(instance.y - position.y) > 0.01) {
      instance.y = position.y;
      mutatedNodeIds.push(instance.id);
    }
  });
}

function stackVersionSectionFrames(frames, mutatedNodeIds) {
  if (frames.length < 2) return;

  const alignedX = frames[0].x;
  let nextY = frames[0].y;
  for (const frame of frames) {
    if (Math.abs(frame.x - alignedX) > 0.01) {
      frame.x = alignedX;
      mutatedNodeIds.push(frame.id);
    }
    if (Math.abs(frame.y - nextY) > 0.01) {
      frame.y = nextY;
      mutatedNodeIds.push(frame.id);
    }
    nextY = frame.y + frame.height + VERSION_SECTION_GAP;
  }
}

function bindDependencyVersionInstance(instance, variable) {
  const alias = figma.variables.createVariableAlias(variable);

  instance.setProperties({
    [DEPENDENCY_VERSION_PROPS.alias]: alias,
    [DEPENDENCY_VERSION_PROPS.number]: alias,
  });
}
