import {
  BIG_MODULE_SIZE,
  CONNECTOR_TEMPLATE_NAME,
  MODULE_DEPENDENCY_TARGETS,
  MODULE_INSTANCE_NAME,
  MODULE_PROPS,
} from "../config/figma-config";
import type { DesignModel, ModuleDependency, ModuleDependencyTarget } from "../domain/design-model";
import type { ModuleDependencySyncGateway } from "../ports/sync-gateways";
import { getComponentPropertyValue, requireSection } from "./figma-node-gateway";
import { updateNamedTextNodes } from "./figma-text-gateway";

type ModuleDependencySlot = {
  key: string;
  moduleName: string;
  role: "parent" | "child";
  groupModule: string;
  dependency?: ModuleDependency;
  x: number;
  y: number;
};

export class FigmaModuleDependencySyncGateway implements ModuleDependencySyncGateway {
  async syncModuleDependencies(designModel: DesignModel) {
    const updatedModules = [];
    const hiddenModules = [];
    const updatedModuleConnectors = [];
    const removedModuleConnectors = [];
    const mutatedNodeIds = [];

    for (const target of MODULE_DEPENDENCY_TARGETS) {
      const dependencies = target.dependencies(designModel);
      if (!Array.isArray(dependencies)) {
        throw new Error(`designModel.content.moduleDependencies.${target.name} is required for module dependency sync.`);
      }

      const section = await requireSection(target.sectionNodeId);
      const existingInstances = collectModuleInstances(section);
      const existingConnectors = collectModuleConnectors(section);
      const template = existingInstances[0];

      if (!template && dependencies.length > 0) {
        throw new Error(`No '${MODULE_INSTANCE_NAME}' module template was found in section '${section.name}'.`);
      }

      const slots = layoutDependencySlots(section, dependencies, existingInstances);
      const assignedInstances = assignModuleInstances(section, slots, existingInstances, existingConnectors, template, mutatedNodeIds);

      for (const slot of slots) {
        const instance = assignedInstances.get(slot.key);
        await updateModuleInstance(instance, slot, mutatedNodeIds);
        updatedModules.push(`${target.name}/${slot.key}`);
      }

      hideUnusedModuleInstances(target, existingInstances, assignedInstances, hiddenModules, mutatedNodeIds);

      const connectorResult = syncModuleConnectors(
        target,
        section,
        dependencies,
        assignedInstances,
        existingConnectors,
        mutatedNodeIds
      );
      updatedModuleConnectors.push(...connectorResult.updatedModuleConnectors);
      removedModuleConnectors.push(...connectorResult.removedModuleConnectors);

      resizeSectionToFit(section, [...assignedInstances.values()]);
    }

    return {
      updatedModules,
      hiddenModules,
      updatedModuleConnectors,
      removedModuleConnectors,
      mutatedNodeIds,
    };
  }
}

function collectModuleInstances(section) {
  return section.findAllWithCriteria({ types: ["INSTANCE"] })
    .filter((instance) => instance.name === MODULE_INSTANCE_NAME)
    .sort((first, second) => first.y - second.y || first.x - second.x);
}

function collectModuleConnectors(section) {
  return section.findAllWithCriteria({ types: ["CONNECTOR"] })
    .filter((connector) => connector.name === CONNECTOR_TEMPLATE_NAME)
    .sort((first, second) => first.y - second.y || first.x - second.x);
}

function moduleNameOf(instance): string | undefined {
  const propertyValue = getComponentPropertyValue(instance, MODULE_PROPS.name);
  if (propertyValue && propertyValue !== MODULE_PROPS.name) {
    return propertyValue;
  }

  return instance.findAllWithCriteria({ types: ["TEXT"] })
    .find((textNode) => textNode.name === "label")
    ?.characters;
}

function layoutDependencySlots(
  section,
  dependencies: ModuleDependency[],
  existingInstances
): ModuleDependencySlot[] {
  const dependenciesByDependencyModule = groupDependenciesByDependencyModule(dependencies);
  const groupModules = [...dependenciesByDependencyModule.keys()].sort((first, second) =>
    groupLevel(first, dependencies).localeCompare(groupLevel(second, dependencies)) ||
      first.localeCompare(second)
  );
  const slots: ModuleDependencySlot[] = [];
  let nextY = MODULE_LAYOUT_PADDING;

  for (const groupModule of groupModules) {
    const groupDependencies = dependenciesByDependencyModule.get(groupModule)
      .sort((first, second) => first.dependentModule.localeCompare(second.dependentModule));
    const parentWidth = widthForModule(groupModule, existingInstances);

    let childX = MODULE_LAYOUT_PADDING;
    let childY = nextY + MODULE_LAYOUT_DEFAULT_HEIGHT + MODULE_PARENT_CHILD_GAP;
    let rowHeight = 0;
    const childSlots: ModuleDependencySlot[] = [];

    for (const dependency of groupDependencies) {
      const childWidth = widthForModule(dependency.dependentModule, existingInstances);
      if (childX > MODULE_LAYOUT_PADDING && childX + childWidth > section.width - MODULE_LAYOUT_PADDING) {
        childX = MODULE_LAYOUT_PADDING;
        childY += rowHeight + MODULE_CHILD_ROW_GAP;
        rowHeight = 0;
      }

      const childSlot: ModuleDependencySlot = {
        key: childSlotKey(dependency),
        moduleName: dependency.dependentModule,
        role: "child",
        groupModule,
        dependency,
        x: childX,
        y: childY,
      };
      childSlots.push(childSlot);

      childX += childWidth + MODULE_LAYOUT_COLUMN_GAP;
      rowHeight = Math.max(rowHeight, MODULE_LAYOUT_DEFAULT_HEIGHT);
    }

    const firstChild = childSlots[0];
    const lastChild = childSlots[childSlots.length - 1];
    const firstChildWidth = firstChild ? widthForModule(firstChild.moduleName, existingInstances) : parentWidth;
    const lastChildWidth = lastChild ? widthForModule(lastChild.moduleName, existingInstances) : parentWidth;
    let parentX = firstChild && lastChild
      ? ((firstChild.x + firstChildWidth / 2) + (lastChild.x + lastChildWidth / 2)) / 2 - parentWidth / 2
      : MODULE_LAYOUT_PADDING;

    if (parentX < MODULE_LAYOUT_PADDING) {
      const offset = MODULE_LAYOUT_PADDING - parentX;
      parentX += offset;
      for (const childSlot of childSlots) {
        childSlot.x += offset;
      }
    }

    slots.push({
      key: parentSlotKey(groupModule),
      moduleName: groupModule,
      role: "parent",
      groupModule,
      x: parentX,
      y: nextY,
    });
    slots.push(...childSlots);

    nextY = childY + Math.max(rowHeight, MODULE_LAYOUT_DEFAULT_HEIGHT) + MODULE_GROUP_GAP;

    if (parentWidth > section.width - MODULE_LAYOUT_PADDING * 2) {
      throw new Error(`Module '${groupModule}' is wider than dependency section '${section.name}'.`);
    }
  }

  return slots;
}

function groupDependenciesByDependencyModule(dependencies: ModuleDependency[]): Map<string, ModuleDependency[]> {
  return dependencies.reduce((groups, dependency) => {
    groups.set(
      dependency.dependencyModule,
      [...(groups.get(dependency.dependencyModule) || []), dependency]
    );
    return groups;
  }, new Map<string, ModuleDependency[]>());
}

function groupLevel(moduleName: string, dependencies: ModuleDependency[]): string {
  const outgoingDependencyCount = dependencies
    .filter((dependency) => dependency.dependentModule === moduleName)
    .length;
  return String(outgoingDependencyCount).padStart(4, "0");
}

function widthForModule(moduleName: string, existingInstances): number {
  const existingInstance = existingInstances.find((instance) => moduleNameOf(instance) === moduleName);
  return Math.max(existingInstance?.width || MODULE_LAYOUT_DEFAULT_WIDTH, MODULE_LAYOUT_DEFAULT_WIDTH);
}

function assignModuleInstances(
  section,
  slots: ModuleDependencySlot[],
  existingInstances,
  existingConnectors,
  template,
  mutatedNodeIds
): Map<string, any> {
  const assignedInstances = new Map();
  const usedInstanceIds = new Set<string>();

  for (const slot of slots) {
    const preferred = findPreferredInstance(slot, existingInstances, existingConnectors, usedInstanceIds);
    const instance = preferred || template.clone();
    if (!preferred) {
      section.appendChild(instance);
      instance.name = MODULE_INSTANCE_NAME;
      mutatedNodeIds.push(instance.id);
    }
    assignedInstances.set(slot.key, instance);
    usedInstanceIds.add(instance.id);
  }

  return assignedInstances;
}

function findPreferredInstance(
  slot: ModuleDependencySlot,
  existingInstances,
  existingConnectors,
  usedInstanceIds: Set<string>
) {
  if (slot.role === "child") {
    const connectedInstance = existingConnectors
      .map((connector) => ({
        start: existingInstances.find((instance) => instance.id === connector.connectorStart?.endpointNodeId),
        end: existingInstances.find((instance) => instance.id === connector.connectorEnd?.endpointNodeId),
      }))
      .find((pair) =>
        pair.start &&
          pair.end &&
          moduleNameOf(pair.start) === slot.moduleName &&
          moduleNameOf(pair.end) === slot.groupModule &&
          !usedInstanceIds.has(pair.start.id)
      )
      ?.start;

    if (connectedInstance) {
      return connectedInstance;
    }
  }

  return existingInstances.find((instance) =>
    moduleNameOf(instance) === slot.moduleName &&
      !usedInstanceIds.has(instance.id)
  );
}

async function updateModuleInstance(instance, slot: ModuleDependencySlot, mutatedNodeIds) {
  requireModuleVariantProperty(instance, MODULE_PROPS.name);
  requireModuleVariantProperty(instance, MODULE_PROPS.size);

  instance.visible = true;
  instance.x = slot.x;
  instance.y = slot.y;
  instance.setProperties({
    [MODULE_PROPS.name]: slot.moduleName,
    [MODULE_PROPS.size]: BIG_MODULE_SIZE,
  });
  mutatedNodeIds.push(instance.id);

  await updateNamedTextNodes(instance, "label", [slot.moduleName], mutatedNodeIds);
}

function requireModuleVariantProperty(moduleInstance, propertyName) {
  const property = moduleInstance.componentProperties?.[propertyName];
  if (!property || property.type !== "VARIANT") {
    throw new Error(
      `Expected '${moduleInstance.id}' to be a '${MODULE_INSTANCE_NAME}' instance with '${propertyName}' variant property.`
    );
  }
}

function hideUnusedModuleInstances(target: ModuleDependencyTarget, existingInstances, assignedInstances, hiddenModules, mutatedNodeIds) {
  const visibleModuleIds = new Set([...assignedInstances.values()].map((instance) => instance.id));
  for (const instance of existingInstances) {
    if (visibleModuleIds.has(instance.id)) continue;
    if (instance.visible === false) continue;

    instance.visible = false;
    hiddenModules.push(`${target.name}/${moduleNameOf(instance) || instance.id}`);
    mutatedNodeIds.push(instance.id);
  }
}

function syncModuleConnectors(
  target: ModuleDependencyTarget,
  section,
  dependencies: ModuleDependency[],
  assignedInstances,
  existingConnectors,
  mutatedNodeIds
) {
  const sortedDependencies = [...dependencies].sort((first, second) =>
    first.dependencyModule.localeCompare(second.dependencyModule) ||
      first.dependentModule.localeCompare(second.dependentModule)
  );
  const template = existingConnectors[0];

  if (!template && sortedDependencies.length > 0) {
    throw new Error(`No '${CONNECTOR_TEMPLATE_NAME}' connector template was found in section '${section.name}'.`);
  }

  const updatedModuleConnectors = [];
  const removedModuleConnectors = [];

  for (let index = 0; index < sortedDependencies.length; index += 1) {
    const dependency = sortedDependencies[index];
    const dependentModuleInstance = assignedInstances.get(childSlotKey(dependency));
    const dependencyModuleInstance = assignedInstances.get(parentSlotKey(dependency.dependencyModule));

    if (!dependentModuleInstance || !dependencyModuleInstance) {
      throw new Error(
        `Cannot create connector for ${target.name}/${dependency.dependentModule} -> ${dependency.dependencyModule}: ` +
          "dependent or dependency module is missing."
      );
    }

    const connector = existingConnectors[index] || template.clone();
    if (!existingConnectors[index]) {
      section.appendChild(connector);
      connector.name = CONNECTOR_TEMPLATE_NAME;
    }
    connector.visible = true;
    connector.connectorStart = {
      endpointNodeId: dependentModuleInstance.id,
      magnet: "TOP",
    };
    connector.connectorEnd = {
      endpointNodeId: dependencyModuleInstance.id,
      magnet: "BOTTOM",
    };
    updatedModuleConnectors.push(`${target.name}/${dependency.dependentModule} -> ${dependency.dependencyModule}`);
    mutatedNodeIds.push(connector.id);
  }

  for (const connector of existingConnectors.slice(sortedDependencies.length)) {
    removedModuleConnectors.push(`${target.name}/${connector.id}`);
    connector.remove();
  }

  return {
    updatedModuleConnectors,
    removedModuleConnectors,
  };
}

function parentSlotKey(moduleName: string): string {
  return `parent:${moduleName}`;
}

function childSlotKey(dependency: ModuleDependency): string {
  return `child:${dependency.dependencyModule}->${dependency.dependentModule}`;
}

function resizeSectionToFit(section, nodes) {
  if (nodes.length === 0) return;

  const maxRight = Math.max(...nodes.map((node) => node.x + node.width));
  const maxBottom = Math.max(...nodes.map((node) => node.y + node.height));
  section.resizeWithoutConstraints(
    Math.max(section.width, maxRight + MODULE_LAYOUT_PADDING),
    Math.max(section.height, maxBottom + MODULE_LAYOUT_PADDING)
  );
}

const MODULE_LAYOUT_PADDING = 100;
const MODULE_LAYOUT_COLUMN_GAP = 80;
const MODULE_PARENT_CHILD_GAP = 128;
const MODULE_CHILD_ROW_GAP = 128;
const MODULE_GROUP_GAP = 96;
const MODULE_LAYOUT_DEFAULT_WIDTH = 340;
const MODULE_LAYOUT_DEFAULT_HEIGHT = 182;
