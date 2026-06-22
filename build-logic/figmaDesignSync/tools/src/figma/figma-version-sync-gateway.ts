import {
  PROJECT_VERSION_COMPONENT_ID,
  VERSION_ALIAS_MODE_NAME,
  VERSION_NUMBER_MODE_NAME,
  VERSION_SECTION_TARGETS,
  VERSIONS_COLLECTION_NAME,
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
} from "./figma-node-gateway";
import { collectText } from "./figma-text-gateway";

export class FigmaVersionSyncGateway implements VersionSyncGateway {
  async syncVersions(designModel: DesignModel) {
    const versionSections = requireVersionSections(designModel);
  const collection = await requireVariableCollection(VERSIONS_COLLECTION_NAME);
  const versionAliasModeId = requireModeId(collection, VERSION_ALIAS_MODE_NAME);
  const versionNumberModeId = requireModeId(collection, VERSION_NUMBER_MODE_NAME);
  const projectVersionComponent = await requireComponent(PROJECT_VERSION_COMPONENT_ID);

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

      const existingInstance = findProjectVersionInstance(parent, versionKey);
      if (!existingInstance) {
        const instance = projectVersionComponent.createInstance();
        const position = nextProjectVersionPosition(parent);

        parent.appendChild(instance);
        instance.x = position.x;
        instance.y = position.y;
        bindProjectVersionInstance(instance, variable);

        mutatedNodeIds.push(instance.id);
        createdInstances.push(versionKey);
      }

      mutatedNodeIds.push(variable.id);
    }

    resizeNodeToFit(parent, parent.children.filter((child) => child.visible !== false), mutatedNodeIds);
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

function findProjectVersionInstance(parent, versionKey) {
  return parent.children.find((child) => {
    if (child.type !== "INSTANCE" || child.name !== ".project version") return false;
    return collectText(child).includes(versionKey);
  });
}

function nextProjectVersionPosition(parent): { x: number; y: number } {
  const instances = parent.children
    .filter((child) => child.type === "INSTANCE" && child.name === ".project version")
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

function bindProjectVersionInstance(instance, variable) {
  const alias = figma.variables.createVariableAlias(variable);

  instance.setProperties({
    "Version alias#63075:0": alias,
    "Version number#63075:1": alias,
  });
}
