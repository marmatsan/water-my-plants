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

    for (const section of versionSections) {
      const target = VERSION_SECTION_TARGETS[section.name];
      if (!target) {
        throw new Error(`No Figma target configured for version section '${section.name}'.`);
      }

      const parent = await requireFrame(target.parentNodeId);
      const entries = Object.entries(section.versions);
      const existingInstances = findDependencyVersionInstances(parent);
      const instancePlan = planDependencyVersionInstanceSync(
        existingInstances,
        entries.map(([versionKey]) => versionKey),
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
          const position = nextDependencyVersionPosition(parent);

          parent.appendChild(instance);
          instance.x = position.x;
          instance.y = position.y;

          mutatedNodeIds.push(instance.id);
          createdInstances.push(versionKey);
        }

        bindDependencyVersionInstance(instance, variable);
        mutatedNodeIds.push(variable.id);
      }

      resizeNodeToFit(parent, parent.children.filter((child) => child.visible !== false), mutatedNodeIds);
      stackAncestorSectionSiblingsWithGap(parent, mutatedNodeIds);
      resizeAncestorSectionsToFit(parent, mutatedNodeIds);
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

function nextDependencyVersionPosition(parent): { x: number; y: number } {
  const instances = parent.children
    .filter((child) => child.type === "INSTANCE" && DEPENDENCY_VERSION_INSTANCE_NAMES.includes(child.name))
    .sort((first, second) => first.y - second.y || first.x - second.x);

  if (instances.length === 0) {
    return { x: 0, y: 0 };
  }

  const columnXs: number[] = [...new Set<number>(instances.map((instance) => Math.round(instance.x)))]
    .sort((first, second) => first - second)
    .slice(0, 2);

  if (columnXs.length === 1) {
    columnXs.push(columnXs[0] + Math.round(instances[0].width) + 64);
  }

  const nextColumnIndex = instances.length % columnXs.length;
  const columnInstances = instances.filter((instance) => Math.round(instance.x) === columnXs[nextColumnIndex]);
  const lastInColumn = columnInstances.at(-1);
  const rowGap = 64;

  return {
    x: columnXs[nextColumnIndex],
    y: lastInColumn ? lastInColumn.y + lastInColumn.height + rowGap : 0,
  };
}

function bindDependencyVersionInstance(instance, variable) {
  const alias = figma.variables.createVariableAlias(variable);

  instance.setProperties({
    [DEPENDENCY_VERSION_PROPS.alias]: alias,
    [DEPENDENCY_VERSION_PROPS.number]: alias,
  });
}
