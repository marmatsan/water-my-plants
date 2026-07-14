import {
  ARTIFACTS_BUNDLE_INSTANCE_NAME,
  ARTIFACTS_BUNDLE_PROPS,
  ARTIFACT_INSTANCE_NAME,
  ARTIFACT_PROPS,
  CATALOG_TREE_TARGETS,
  CI_DOCUMENTATION_PAGE_ID,
  CI_NODE_COMPONENT_ID,
  CI_NODE_PROPS,
  CI_VARIABLE_COLLECTION_NAME,
  CI_VARIABLE_MODE_NAMES,
  HEADER_INSTANCE_NAME,
  HEADER_LINK_PROPERTY_NAME,
  HEADER_SECTION_TARGETS,
  METADATA_PAGE_ID,
  PROJECT_VERSION_COMPONENT_ID,
  TOOL_ARTIFACT_USAGE_INSTANCE_NAME,
  TOOL_ARTIFACT_USAGE_PROPS,
  TREE_NODE_COMPONENT_IDS,
  TREE_NODE_PROPS,
  USAGE_CHIP_COMPONENT_SET_ID,
  USAGE_CHIP_INSTANCE_NAME,
  USAGE_CHIP_KINDS,
  USAGE_CHIP_PROPS,
  VERSION_ALIAS_MODE_NAME,
  VERSION_NUMBER_MODE_NAME,
  VERSION_SECTION_TARGETS,
  VERSIONS_COLLECTION_NAMES,
} from "../config/figma-config";
import { CI_VISUAL_TARGET_NAMES } from "../domain/ci/create-ci-visual-plan";
import { flattenCatalogNodes, requireUniqueLabels } from "../domain/catalog/flatten-catalog-nodes";
import {
  libraryArtifacts,
  libraryBundles,
} from "../domain/catalog/library-catalog-entries";
import type { DesignModel } from "../domain/design-model";
import type {
  VisualContractCheckGateway,
  VisualContractCheckOptions,
} from "../ports/sync-gateways";
import { filterModelRoots } from "./figma-catalog-tree-sync-gateway";
import {
  requireComponent,
  requireFrameOrSection,
  requireModeId,
  requireOutlineColorVariable,
  requirePage,
  requireSection,
  requireVariableCollection,
} from "./figma-node-gateway";

export class FigmaVisualContractCheckGateway implements VisualContractCheckGateway {
  async checkVisualContract(
    designModel: DesignModel,
    options: VisualContractCheckOptions = {}
  ) {
    const checkedComponents: string[] = [];
    const checkedSections: string[] = [];
    const checkedVariables: string[] = [];
    const checkedTargets: string[] = [];

    const metadataPage = await requirePage(METADATA_PAGE_ID);
    checkedSections.push(`metadata:${metadataPage.id}`);

    await checkVersionContract(checkedComponents, checkedSections, checkedVariables);
    await checkHeaderContract(checkedComponents, checkedSections);
    await checkCatalogTreeContract(designModel, options, checkedComponents, checkedSections, checkedTargets);
    await checkCiDocumentationContract(options, checkedComponents, checkedSections, checkedVariables, checkedTargets);

    return {
      checkedComponents,
      checkedSections,
      checkedVariables,
      checkedTargets,
      mutatedNodeIds: [],
    };
  }
}

async function checkCiDocumentationContract(
  options,
  checkedComponents,
  checkedSections,
  checkedVariables,
  checkedTargets
) {
  const requestedTargets = options.targetNames == null
    ? [...CI_VISUAL_TARGET_NAMES]
    : options.targetNames.filter((target) => CI_VISUAL_TARGET_NAMES.includes(target));
  if (requestedTargets.length === 0) return;

  const page = await requirePage(CI_DOCUMENTATION_PAGE_ID);
  checkedSections.push(`ciDocumentation:${page.id}`);

  const component = await requireComponent(CI_NODE_COMPONENT_ID);
  requireComponentProperty(component, CI_NODE_PROPS.name, "TEXT");
  requireComponentProperty(component, CI_NODE_PROPS.description, "TEXT");
  requireComponentProperty(component, CI_NODE_PROPS.steps, "TEXT");
  requireComponentProperty(component, CI_NODE_PROPS.source, "TEXT");
  requireComponentProperty(component, CI_NODE_PROPS.showSteps, "BOOLEAN");
  requireComponentProperty(component, CI_NODE_PROPS.showSource, "BOOLEAN");
  checkedComponents.push(`${component.name}:${component.id}`);

  const collection = await requireVariableCollection(CI_VARIABLE_COLLECTION_NAME);
  for (const modeName of CI_VARIABLE_MODE_NAMES) {
    requireModeId(collection, modeName);
  }
  checkedVariables.push(collection.name);
  checkedTargets.push(...requestedTargets);
}

async function checkHeaderContract(checkedComponents, checkedSections) {
  for (const target of HEADER_SECTION_TARGETS) {
    const section = await requireSection(target.sectionNodeId);
    const header = section.children.find(
      (child) => child.type === "INSTANCE" && child.name === HEADER_INSTANCE_NAME
    );
    if (!header || header.type !== "INSTANCE") {
      throw new Error(`Section '${section.id}' is missing a direct '${HEADER_INSTANCE_NAME}' instance.`);
    }
    requireComponentProperty(header, HEADER_LINK_PROPERTY_NAME, "TEXT");
    const linkTexts = header.findAllWithCriteria({ types: ["TEXT"] })
      .filter((text) => text.name === HEADER_LINK_PROPERTY_NAME);
    if (linkTexts.length !== 1) {
      throw new Error(`Header '${header.id}' expected one '${HEADER_LINK_PROPERTY_NAME}' text node, found ${linkTexts.length}.`);
    }
    checkedComponents.push(`${HEADER_INSTANCE_NAME}:${header.id}`);
    checkedSections.push(`header:${section.id}`);
  }
}

async function checkVersionContract(checkedComponents, checkedSections, checkedVariables) {
  const collection = await requireVariableCollection(VERSIONS_COLLECTION_NAMES);
  requireModeId(collection, VERSION_ALIAS_MODE_NAME);
  requireModeId(collection, VERSION_NUMBER_MODE_NAME);
  checkedVariables.push(collection.name);

  const projectVersionComponent = await requireComponent(PROJECT_VERSION_COMPONENT_ID);
  checkedComponents.push(`${projectVersionComponent.name}:${projectVersionComponent.id}`);
  const outlineVariable = await requireOutlineColorVariable();
  checkedVariables.push(outlineVariable.name);

  for (const [sectionName, sectionTarget] of Object.entries(VERSION_SECTION_TARGETS)) {
    const sectionFrame = await requireFrameOrSection(sectionTarget.parentNodeId);
    checkedSections.push(`${sectionName}:${sectionFrame.id}`);
  }
}

async function checkCatalogTreeContract(
  designModel,
  options,
  checkedComponents,
  checkedSections,
  checkedTargets
) {
  const targets = resolveCatalogTargets(options.targetNames);
  const requiredTreeNodeTypes = new Set(targets.map((target) => target.type));

  if (requiredTreeNodeTypes.has("Library")) {
    await checkLibraryTreeNodeComponent(checkedComponents);
  }
  if (requiredTreeNodeTypes.has("Plugin")) {
    await checkPluginTreeNodeComponent(checkedComponents);
  }
  if (requiredTreeNodeTypes.size > 0) {
    await checkUsageChipComponentSet(checkedComponents);
  }

  for (const target of targets) {
    const modelNodes = target.nodes(designModel);
    const sectionNodeId = options.sectionNodeOverrides?.[target.name] || target.sectionNodeId;

    if (modelNodes == null) {
      if (target.lifecycle === "stableDocumentationTarget") {
        const section = await requireSection(sectionNodeId);
        checkedSections.push(`${target.name}:${section.id}`);
        checkedTargets.push(target.name);
      }
      continue;
    }

    if (!Array.isArray(modelNodes)) {
      throw new Error(`designModel.content.catalogs.${target.name} is required for visual contract preflight.`);
    }

    const rootFilter = options.rootFilters?.[target.name];
    const scopedModelNodes = filterModelRoots(target, modelNodes, rootFilter);
    const expectedNodes = flattenCatalogNodes(scopedModelNodes, target.type);
    requireUniqueLabels(target, expectedNodes);

    if (target.lifecycle === "declaredCatalogTarget" && expectedNodes.length === 0) {
      checkedTargets.push(target.name);
      continue;
    }

    const section = await requireSection(sectionNodeId);
    checkedSections.push(`${target.name}:${section.id}`);
    if (target.type === "Library") {
      await checkLibraryTemplatesForTarget(section, expectedNodes, checkedComponents);
    }
    checkedTargets.push(target.name);
  }
}

async function checkLibraryTreeNodeComponent(checkedComponents) {
  const component = await requireComponent(TREE_NODE_COMPONENT_IDS.Library);
  checkedComponents.push(`${component.name}:${component.id}`);
  requireComponentProperty(component, TREE_NODE_PROPS.libraryGroup, "TEXT");
  requireComponentProperty(component, TREE_NODE_PROPS.showArtifacts, "BOOLEAN");
  requireComponentProperty(component, TREE_NODE_PROPS.type, "VARIANT");
}

async function checkLibraryTemplatesForTarget(section, expectedNodes, checkedComponents) {
  const artifacts = expectedNodes.flatMap((node) => libraryArtifacts(node.entries));
  const bundles = expectedNodes.flatMap((node) => libraryBundles(node.entries));
  const requiresArtifactTemplate = artifacts.length > 0 ||
    bundles.some((bundle) => (bundle.artifacts || []).length > 0);
  const requiresBundleTemplate = bundles.length > 0;
  const requiresToolArtifactUsage = artifacts.some((artifact) =>
    (artifact.configuredByConventionPlugins || []).length > 0
  );

  if (requiresArtifactTemplate) {
    const artifact = await findLibraryTemplateInstance(section, ARTIFACT_INSTANCE_NAME);
    checkArtifactTemplate(artifact, checkedComponents);
    if (requiresToolArtifactUsage) {
      const toolArtifactUsage = requireNestedInstance(artifact, TOOL_ARTIFACT_USAGE_INSTANCE_NAME);
      requireComponentProperty(toolArtifactUsage, TOOL_ARTIFACT_USAGE_PROPS.target, "TEXT");
      checkedComponents.push(`${TOOL_ARTIFACT_USAGE_INSTANCE_NAME}:${toolArtifactUsage.id}`);
    }
  }

  if (requiresBundleTemplate) {
    const bundle = await findLibraryTemplateInstance(section, ARTIFACTS_BUNDLE_INSTANCE_NAME);
    checkBundleTemplate(bundle, checkedComponents);
    checkArtifactTemplate(requireNestedInstance(bundle, ARTIFACT_INSTANCE_NAME), checkedComponents);
  }
}

async function findLibraryTemplateInstance(section, instanceName) {
  const sectionTemplate = section.findAllWithCriteria({ types: ["INSTANCE"] })
    .find((candidate) => candidate.name === instanceName);
  if (sectionTemplate) return sectionTemplate;

  const component = await requireComponent(TREE_NODE_COMPONENT_IDS.Library);
  return requireNestedInstance(component, instanceName);
}

function checkArtifactTemplate(artifact, checkedComponents) {
  requireComponentProperty(artifact, ARTIFACT_PROPS.name, "TEXT");
  requireComponentProperty(artifact, ARTIFACT_PROPS.version, "TEXT");
  requireComponentProperty(artifact, ARTIFACT_PROPS.showVersion, "BOOLEAN");
  requireComponentProperty(artifact, ARTIFACT_PROPS.showAppliedByPlugin, "BOOLEAN");
  requireComponentProperty(artifact, ARTIFACT_PROPS.showUsedByModule, "BOOLEAN");
  requireComponentProperty(artifact, ARTIFACT_PROPS.showConfiguredAsTool, "BOOLEAN");
  checkedComponents.push(`${ARTIFACT_INSTANCE_NAME}:${artifact.id}`);
}

function checkBundleTemplate(bundle, checkedComponents) {
  requireComponentProperty(bundle, ARTIFACTS_BUNDLE_PROPS.alias, "TEXT");
  requireComponentProperty(bundle, ARTIFACTS_BUNDLE_PROPS.version, "TEXT");
  requireComponentProperty(bundle, ARTIFACTS_BUNDLE_PROPS.showVersion, "BOOLEAN");
  requireComponentProperty(bundle, ARTIFACTS_BUNDLE_PROPS.showAppliedByPlugin, "BOOLEAN");
  requireComponentProperty(bundle, ARTIFACTS_BUNDLE_PROPS.showUsedByModule, "BOOLEAN");
  checkedComponents.push(`${ARTIFACTS_BUNDLE_INSTANCE_NAME}:${bundle.id}`);
}

async function checkPluginTreeNodeComponent(checkedComponents) {
  const component = await requireComponent(TREE_NODE_COMPONENT_IDS.Plugin);
  checkedComponents.push(`${component.name}:${component.id}`);
  requireComponentProperty(component, TREE_NODE_PROPS.pluginId, "TEXT");
  requireComponentProperty(component, TREE_NODE_PROPS.pluginVersion, "TEXT");
  requireComponentProperty(component, TREE_NODE_PROPS.showPluginVersion, "BOOLEAN");
  requireComponentProperty(component, TREE_NODE_PROPS.showAppliedByModule, "BOOLEAN");
  requireComponentProperty(component, TREE_NODE_PROPS.showUsedByConventionPlugin, "BOOLEAN");
  requireComponentProperty(component, TREE_NODE_PROPS.showUnused, "BOOLEAN");
  requireComponentProperty(component, TREE_NODE_PROPS.showIsGradlePlugin, "BOOLEAN");
  requireComponentProperty(component, TREE_NODE_PROPS.type, "VARIANT");
}

async function checkUsageChipComponentSet(checkedComponents) {
  const componentSet = await figma.getNodeByIdAsync(USAGE_CHIP_COMPONENT_SET_ID);
  if (!componentSet || componentSet.type !== "COMPONENT_SET") {
    throw new Error(`Expected '${USAGE_CHIP_COMPONENT_SET_ID}' to be the '${USAGE_CHIP_INSTANCE_NAME}' COMPONENT_SET.`);
  }

  for (const kind of Object.values(USAGE_CHIP_KINDS)) {
    const variant = componentSet.children
      .filter((candidate) => candidate.type === "COMPONENT")
      .find((candidate) => candidate.variantProperties?.[USAGE_CHIP_PROPS.kind] === kind);
    if (!variant) {
      throw new Error(`Expected '${USAGE_CHIP_INSTANCE_NAME}' component set to contain kind='${kind}'.`);
    }
  }

  checkedComponents.push(`${componentSet.name}:${componentSet.id}`);
}

function resolveCatalogTargets(targetNames?: string[]) {
  const knownTargetNames = new Set(
    CATALOG_TREE_TARGETS.flatMap((target) => [target.name, ...(target.aliases || [])])
  );
  const scopedTargetNames = targetNames?.filter((targetName) => knownTargetNames.has(targetName));
  const targetNameSet = new Set<string>(scopedTargetNames || CATALOG_TREE_TARGETS.map((target) => target.name));

  return CATALOG_TREE_TARGETS.filter((target) =>
    targetNameSet.has(target.name) || (target.aliases || []).some((alias) => targetNameSet.has(alias))
  );
}

function requireNestedInstance(root: any, instanceName: string) {
  const instance = root.findAllWithCriteria({ types: ["INSTANCE"] })
    .find((candidate) => candidate.name === instanceName);
  if (!instance) {
    throw new Error(`Expected '${root.id}' to contain a '${instanceName}' template instance.`);
  }
  return instance;
}

function requireComponentProperty(node: any, propertyName: string, propertyType: string) {
  const properties = componentPropertiesForPreflight(node);
  const propertyEntry = Object.entries(properties)
    .find(([key, property]) =>
      (key === propertyName || key.startsWith(`${componentPropertyName(propertyName)}#`)) &&
        (!propertyType || property.type === propertyType)
    );

  if (!propertyEntry) {
    throw new Error(
      `Node '${node.id}' is missing ${propertyType} component property '${componentPropertyName(propertyName)}'.`
    );
  }
}

function componentPropertiesForPreflight(node: any): Record<string, any> {
  if (node.type === "INSTANCE") {
    return node.componentProperties || {};
  }

  if (node.type === "COMPONENT" && node.parent?.type === "COMPONENT_SET") {
    return node.parent.componentPropertyDefinitions || {};
  }

  if (node.type === "COMPONENT" || node.type === "COMPONENT_SET") {
    return node.componentPropertyDefinitions || {};
  }

  return {};
}

function componentPropertyName(propertyName) {
  return propertyName.split("#")[0];
}
