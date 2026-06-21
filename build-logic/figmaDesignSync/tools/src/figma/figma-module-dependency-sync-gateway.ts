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

type ModulePosition = {
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
      const modules = sortedModules(dependencies);
      const moduleInstances = collectModuleInstances(section);
      const moduleInstancesByName = collectCanonicalModuleInstancesByName(moduleInstances);
      const template = moduleInstances[0];

      if (!template && modules.length > 0) {
        throw new Error(`No '${MODULE_INSTANCE_NAME}' module template was found in section '${section.name}'.`);
      }

      for (const moduleName of modules) {
        if (moduleInstancesByName.has(moduleName)) continue;

        const instance = template.clone();
        section.appendChild(instance);
        instance.name = MODULE_INSTANCE_NAME;
        moduleInstancesByName.set(moduleName, instance);
        moduleInstances.push(instance);
        mutatedNodeIds.push(instance.id);
      }

      const positionsByModule = layoutModules(target, section, dependencies, modules, moduleInstancesByName);

      for (const moduleName of modules) {
        const instance = moduleInstancesByName.get(moduleName);
        await updateModuleInstance(instance, moduleName, positionsByModule.get(moduleName), mutatedNodeIds);
        updatedModules.push(`${target.name}/${moduleName}`);
      }

      const visibleModuleIds = new Set([...moduleInstancesByName.values()].map((instance) => instance.id));
      for (const instance of moduleInstances) {
        if (visibleModuleIds.has(instance.id) && modules.includes(moduleNameOf(instance))) continue;
        if (instance.visible === false) continue;

        instance.visible = false;
        hiddenModules.push(`${target.name}/${moduleNameOf(instance) || instance.id}`);
        mutatedNodeIds.push(instance.id);
      }

      const connectorResult = syncModuleConnectors(
        target,
        section,
        dependencies,
        moduleInstancesByName,
        mutatedNodeIds
      );
      updatedModuleConnectors.push(...connectorResult.updatedModuleConnectors);
      removedModuleConnectors.push(...connectorResult.removedModuleConnectors);
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

function sortedModules(dependencies: ModuleDependency[]): string[] {
  return [
    ...new Set(dependencies.flatMap((dependency) => [
      dependency.dependentModule,
      dependency.dependencyModule,
    ])),
  ].sort();
}

function collectModuleInstances(section) {
  return section.findAllWithCriteria({ types: ["INSTANCE"] })
    .filter((instance) => instance.name === MODULE_INSTANCE_NAME)
    .sort((first, second) => first.y - second.y || first.x - second.x);
}

function collectCanonicalModuleInstancesByName(moduleInstances) {
  const instancesByName = new Map();

  for (const instance of moduleInstances) {
    const moduleName = moduleNameOf(instance);
    if (!moduleName || instancesByName.has(moduleName)) continue;

    instancesByName.set(moduleName, instance);
  }

  return instancesByName;
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

function layoutModules(
  target: ModuleDependencyTarget,
  section,
  dependencies: ModuleDependency[],
  modules: string[],
  moduleInstancesByName
): Map<string, ModulePosition> {
  const levelsByModule = moduleLevels(dependencies, modules);
  const modulesByLevel = new Map<number, string[]>();

  for (const moduleName of modules) {
    const level = levelsByModule.get(moduleName) || 0;
    modulesByLevel.set(level, [...(modulesByLevel.get(level) || []), moduleName]);
  }

  const positionsByModule = new Map<string, ModulePosition>();
  const levels = [...modulesByLevel.keys()].sort((first, second) => first - second);
  let currentY = MODULE_LAYOUT_PADDING;

  for (const level of levels) {
    const levelModules = modulesByLevel.get(level).sort();
    let currentX = MODULE_LAYOUT_PADDING;
    let rowHeight = 0;

    for (const moduleName of levelModules) {
      const instance = moduleInstancesByName.get(moduleName);
      const instanceWidth = Math.max(instance?.width || MODULE_LAYOUT_DEFAULT_WIDTH, MODULE_LAYOUT_DEFAULT_WIDTH);
      const instanceHeight = instance?.height || MODULE_LAYOUT_DEFAULT_HEIGHT;

      if (currentX > MODULE_LAYOUT_PADDING && currentX + instanceWidth > section.width - MODULE_LAYOUT_PADDING) {
        currentX = MODULE_LAYOUT_PADDING;
        currentY += rowHeight + MODULE_LAYOUT_ROW_WRAP_GAP;
        rowHeight = 0;
      }

      positionsByModule.set(moduleName, {
        x: currentX,
        y: currentY,
      });

      currentX += instanceWidth + MODULE_LAYOUT_COLUMN_GAP;
      rowHeight = Math.max(rowHeight, instanceHeight);
    }

    currentY += rowHeight + MODULE_LAYOUT_LEVEL_GAP;
  }

  if (positionsByModule.size !== modules.length) {
    throw new Error(`${target.name} module dependency layout did not assign every module.`);
  }

  return positionsByModule;
}

function moduleLevels(
  dependencies: ModuleDependency[],
  modules: string[]
): Map<string, number> {
  const dependenciesByModule = new Map<string, string[]>();
  for (const dependency of dependencies) {
    dependenciesByModule.set(
      dependency.dependentModule,
      [...(dependenciesByModule.get(dependency.dependentModule) || []), dependency.dependencyModule]
    );
  }

  const levelsByModule = new Map<string, number>();
  const visiting = new Set<string>();

  const levelOf = (moduleName: string): number => {
    if (levelsByModule.has(moduleName)) return levelsByModule.get(moduleName);
    if (visiting.has(moduleName)) return 0;

    visiting.add(moduleName);
    const dependencyLevels = (dependenciesByModule.get(moduleName) || [])
      .map((dependencyModule) => levelOf(dependencyModule));
    visiting.delete(moduleName);

    const level = dependencyLevels.length === 0
      ? 0
      : Math.max(...dependencyLevels) + 1;
    levelsByModule.set(moduleName, level);
    return level;
  };

  for (const moduleName of modules) {
    levelOf(moduleName);
  }

  return levelsByModule;
}

async function updateModuleInstance(instance, moduleName, position, mutatedNodeIds) {
  requireModuleVariantProperty(instance, MODULE_PROPS.name);
  requireModuleVariantProperty(instance, MODULE_PROPS.size);

  instance.visible = true;
  instance.x = position.x;
  instance.y = position.y;
  instance.setProperties({
    [MODULE_PROPS.name]: moduleName,
    [MODULE_PROPS.size]: BIG_MODULE_SIZE,
  });
  mutatedNodeIds.push(instance.id);

  await updateNamedTextNodes(instance, "label", [moduleName], mutatedNodeIds);
}

function requireModuleVariantProperty(moduleInstance, propertyName) {
  const property = moduleInstance.componentProperties?.[propertyName];
  if (!property || property.type !== "VARIANT") {
    throw new Error(
      `Expected '${moduleInstance.id}' to be a '${MODULE_INSTANCE_NAME}' instance with '${propertyName}' variant property.`
    );
  }
}

function syncModuleConnectors(
  target: ModuleDependencyTarget,
  section,
  dependencies: ModuleDependency[],
  moduleInstancesByName,
  mutatedNodeIds
) {
  const connectors = section.findAllWithCriteria({ types: ["CONNECTOR"] })
    .filter((connector) => connector.name === CONNECTOR_TEMPLATE_NAME)
    .sort((first, second) => first.y - second.y || first.x - second.x);
  const template = connectors[0];

  if (!template && dependencies.length > 0) {
    throw new Error(`No '${CONNECTOR_TEMPLATE_NAME}' connector template was found in section '${section.name}'.`);
  }

  const sortedDependencies = [...dependencies].sort((first, second) =>
    first.dependentModule.localeCompare(second.dependentModule) ||
      first.dependencyModule.localeCompare(second.dependencyModule)
  );
  const updatedModuleConnectors = [];
  const removedModuleConnectors = [];

  for (let index = 0; index < sortedDependencies.length; index += 1) {
    const dependency = sortedDependencies[index];
    const dependentModuleInstance = moduleInstancesByName.get(dependency.dependentModule);
    const dependencyModuleInstance = moduleInstancesByName.get(dependency.dependencyModule);

    if (!dependentModuleInstance || !dependencyModuleInstance) {
      throw new Error(
        `Cannot create connector for ${target.name}/${dependency.dependentModule} -> ${dependency.dependencyModule}: ` +
          "dependent or dependency module is missing."
      );
    }

    const connector = connectors[index] || template.clone();
    if (!connectors[index]) {
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

  for (const connector of connectors.slice(sortedDependencies.length)) {
    removedModuleConnectors.push(`${target.name}/${connector.id}`);
    connector.remove();
  }

  return {
    updatedModuleConnectors,
    removedModuleConnectors,
  };
}

const MODULE_LAYOUT_PADDING = 100;
const MODULE_LAYOUT_COLUMN_GAP = 80;
const MODULE_LAYOUT_LEVEL_GAP = 170;
const MODULE_LAYOUT_ROW_WRAP_GAP = 100;
const MODULE_LAYOUT_DEFAULT_WIDTH = 340;
const MODULE_LAYOUT_DEFAULT_HEIGHT = 182;
