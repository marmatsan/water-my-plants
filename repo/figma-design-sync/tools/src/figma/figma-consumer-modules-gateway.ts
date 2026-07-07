import {
  ARTIFACT_INSTANCE_NAME,
  ARTIFACTS_BUNDLE_INSTANCE_NAME,
  ARTIFACTS_BUNDLE_PROPS,
  ARTIFACT_PROPS,
  USAGE_CHIP_COMPONENT_SET_ID,
  USAGE_CHIP_INSTANCE_NAME,
  USAGE_CHIP_KINDS,
  USAGE_CHIP_PROPS,
} from "../config/figma-config";
import { sortedUnique } from "../domain/catalog/library-catalog-entries";
import { loadTextNodeFonts } from "./figma-text-gateway";

type ConsumerModuleOptions = {
  excludeArtifactDescendants?: boolean;
};

export async function updateLibraryArtifactConsumerModules(root, artifacts, mutatedNodeIds) {
  const artifactInstances = root.findAllWithCriteria({ types: ["INSTANCE"] })
    .filter((candidate) => candidate.name === ARTIFACT_INSTANCE_NAME);

  if (artifactInstances.length < artifacts.length) {
    throw new Error(
      `Tree node '${root.id}' expected at least ${artifacts.length} '${ARTIFACT_INSTANCE_NAME}' instances, ` +
        `found ${artifactInstances.length}. Update the .tree node component structure before writing metadata.`
    );
  }

  for (let index = 0; index < artifacts.length; index += 1) {
    const artifactInstance = artifactInstances[index];
    const artifact = artifacts[index];
    const providedByConventionPlugins = usageChipsForConventionPlugins(artifact.providedByConventionPlugins || []);
    artifactInstance.visible = true;
    artifactInstance.setProperties({
      [ARTIFACT_PROPS.showConsumerModules]: artifact.requiredByModules.length > 0 || providedByConventionPlugins.length > 0,
    });
    mutatedNodeIds.push(artifactInstance.id);
    await updateUsageChipInstances(
      artifactInstance,
      "Provided by",
      providedByConventionPlugins,
      mutatedNodeIds
    );
    await updateConsumerModuleInstances(
      artifactInstance,
      "Required by",
      artifact.requiredByModules,
      mutatedNodeIds
    );
  }

  hideUnusedInstances(artifactInstances.slice(artifacts.length), mutatedNodeIds);
}

export async function updateLibraryBundleConsumerModules(root, bundles, mutatedNodeIds) {
  const bundleInstances = root.findAllWithCriteria({ types: ["INSTANCE"] })
    .filter((candidate) => candidate.name === ARTIFACTS_BUNDLE_INSTANCE_NAME);

  if (bundleInstances.length < bundles.length) {
    throw new Error(
      `Tree node '${root.id}' expected at least ${bundles.length} '${ARTIFACTS_BUNDLE_INSTANCE_NAME}' instances, ` +
        `found ${bundleInstances.length}. Update the .tree node component structure before writing metadata.`
    );
  }

  for (let index = 0; index < bundles.length; index += 1) {
    const bundleInstance = bundleInstances[index];
    const bundle = bundles[index];
    const providedByConventionPlugins = usageChipsForConventionPlugins(bundle.providedByConventionPlugins || []);
    bundleInstance.visible = true;
    bundleInstance.setProperties({
      [ARTIFACTS_BUNDLE_PROPS.showConsumerModules]: bundle.requiredByModules.length > 0 || providedByConventionPlugins.length > 0,
    });
    mutatedNodeIds.push(bundleInstance.id);
    await updateUsageChipInstances(
      bundleInstance,
      "Provided by",
      providedByConventionPlugins,
      mutatedNodeIds,
      { excludeArtifactDescendants: true }
    );
    await updateConsumerModuleInstances(
      bundleInstance,
      "Required by",
      bundle.requiredByModules,
      mutatedNodeIds,
      { excludeArtifactDescendants: true }
    );
  }

  hideUnusedInstances(bundleInstances.slice(bundles.length), mutatedNodeIds);
}

export async function updateConsumerModuleInstances(
  root,
  heading,
  modules,
  mutatedNodeIds,
  options: ConsumerModuleOptions = {}
) {
  await updateUsageChipInstances(
    root,
    heading,
    modules.map((moduleName) => ({
      kind: USAGE_CHIP_KINDS.module,
      name: moduleName,
    })),
    mutatedNodeIds,
    options
  );
}

export async function updateUsageChipInstances(
  root,
  heading,
  usages,
  mutatedNodeIds,
  options: ConsumerModuleOptions = {}
) {
  if (usages.length > 0) {
    requireUsageChipHeading(root, heading);
  }

  const usageChipInstances = root.findAllWithCriteria({ types: ["INSTANCE"] })
    .filter((candidate) => candidate.name === USAGE_CHIP_INSTANCE_NAME)
    .filter((candidate) => belongsToHeadingUsageChipBlock(candidate, root, heading))
    .filter((candidate) => !options.excludeArtifactDescendants || !hasAncestorInstanceNamed(candidate, ARTIFACT_INSTANCE_NAME, root));

  if (usageChipInstances.length < usages.length) {
    throw new Error(
      `Node '${root.id}' expected at least ${usages.length} '${USAGE_CHIP_INSTANCE_NAME}' instances for '${heading}', ` +
        `found ${usageChipInstances.length}. Update the .tree node component structure before writing metadata.`
    );
  }

  for (let index = 0; index < usages.length; index += 1) {
    const usageChipInstance = usageChipInstances[index];
    await setUsageChipInstance(usageChipInstance, usages[index], mutatedNodeIds);
  }

  for (const usageChipInstance of usageChipInstances.slice(usages.length)) {
    hideInstance(usageChipInstance, mutatedNodeIds);
  }
}

function hasAncestorInstanceNamed(node, name, boundary) {
  let current = node.parent;
  while (current && current.id !== boundary.id) {
    if (current.type === "INSTANCE" && current.name === name) return true;
    current = current.parent;
  }
  return false;
}

function requireUsageChipHeading(root, heading) {
  const hasHeading = findUsageChipHeading(root, heading) !== undefined;

  if (!hasHeading) {
    throw new Error(`Node '${root.id}' is missing '${heading}' usage chip heading text.`);
  }
}

async function setUsageChipInstance(usageChipInstance, usage, mutatedNodeIds) {
  const kindPropertyKey = findUsageChipProperty(usageChipInstance, USAGE_CHIP_PROPS.kind, "VARIANT");
  const namePropertyKey = findUsageChipProperty(usageChipInstance, USAGE_CHIP_PROPS.name, "TEXT");

  usageChipInstance.visible = true;
  usageChipInstance.name = USAGE_CHIP_INSTANCE_NAME;

  if (kindPropertyKey) {
    usageChipInstance.setProperties({ [kindPropertyKey]: usage.kind });
  } else {
    await swapUsageChipKind(usageChipInstance, usage.kind, mutatedNodeIds);
  }

  if (namePropertyKey) {
    usageChipInstance.setProperties({ [namePropertyKey]: usage.name });
  }

  await updateUsageChipLabelText(usageChipInstance, usage.name, mutatedNodeIds);
  mutatedNodeIds.push(usageChipInstance.id);
  await resizeUsageChipInstanceToFitLabel(usageChipInstance, mutatedNodeIds);
  resizeAncestorContainersToFit(usageChipInstance, mutatedNodeIds);
}

async function swapUsageChipKind(usageChipInstance, kind, mutatedNodeIds) {
  const variant = await requireUsageChipVariant(kind);
  const mainComponent = await usageChipInstance.getMainComponentAsync();
  if (mainComponent?.id === variant.id) return;

  usageChipInstance.swapComponent(variant);
  usageChipInstance.name = USAGE_CHIP_INSTANCE_NAME;
  mutatedNodeIds.push(usageChipInstance.id);
}

async function requireUsageChipVariant(kind) {
  const componentSet = await figma.getNodeByIdAsync(USAGE_CHIP_COMPONENT_SET_ID);
  if (!componentSet || componentSet.type !== "COMPONENT_SET") {
    throw new Error(`Expected '${USAGE_CHIP_COMPONENT_SET_ID}' to be the '${USAGE_CHIP_INSTANCE_NAME}' COMPONENT_SET.`);
  }

  const variant = componentSet.children
    .filter((candidate) => candidate.type === "COMPONENT")
    .find((candidate) => candidate.variantProperties?.[USAGE_CHIP_PROPS.kind] === kind);
  if (!variant) {
    throw new Error(`Expected '${USAGE_CHIP_INSTANCE_NAME}' component set to contain kind='${kind}'.`);
  }

  return variant;
}

async function updateUsageChipLabelText(usageChipInstance, name, mutatedNodeIds) {
  const label = usageChipInstance.findAllWithCriteria({ types: ["TEXT"] })
    .find((textNode) => textNode.name === "label");
  if (!label) {
    throw new Error(`Expected '${usageChipInstance.id}' to contain a 'label' text node.`);
  }

  if (label.characters === name) return;

  await loadTextNodeFonts(label);
  label.characters = name;
  mutatedNodeIds.push(label.id);
}

function belongsToHeadingUsageChipBlock(candidate, root, heading) {
  const headingNode = findUsageChipHeading(root, heading);
  if (!headingNode) return false;

  const container = nearestAncestorWithUsageChips(headingNode, root) || root;
  return candidate.id === container.id || hasAncestor(candidate, container);
}

function findUsageChipHeading(root, heading) {
  return root.findAllWithCriteria({ types: ["TEXT"] })
    .find((textNode) => textNode.name === "label" && textNode.characters === heading);
}

function nearestAncestorWithUsageChips(node, boundary) {
  let current = node.parent;
  while (current && current.id !== boundary.id) {
    if ("findAllWithCriteria" in current) {
      const hasUsageChips = current.findAllWithCriteria({ types: ["INSTANCE"] })
        .some((candidate) => candidate.name === USAGE_CHIP_INSTANCE_NAME);
      if (hasUsageChips) return current;
    }
    current = current.parent;
  }
  return boundary;
}

function hasAncestor(node, ancestor) {
  let current = node.parent;
  while (current) {
    if (current.id === ancestor.id) return true;
    current = current.parent;
  }
  return false;
}

async function resizeUsageChipInstanceToFitLabel(usageChipInstance, mutatedNodeIds) {
  const label = usageChipInstance.findAllWithCriteria({ types: ["TEXT"] })
    .find((textNode) => textNode.name === "label");
  if (!label) return;

  await loadTextNodeFonts(label);
  label.textAutoResize = "WIDTH_AND_HEIGHT";

  const targetWidth = Math.ceil(label.x + label.width + MODULE_HORIZONTAL_PADDING);
  const targetHeight = Math.ceil(Math.max(usageChipInstance.height, label.y + label.height + MODULE_VERTICAL_PADDING));
  if (usageChipInstance.width < targetWidth || usageChipInstance.height < targetHeight) {
    usageChipInstance.resizeWithoutConstraints(
      Math.max(usageChipInstance.width, targetWidth),
      Math.max(usageChipInstance.height, targetHeight)
    );
    mutatedNodeIds.push(usageChipInstance.id);
  }
}

function resizeAncestorContainersToFit(node, mutatedNodeIds) {
  let child = node;
  let parent = child.parent;
  let requiredWidth = child.width;
  let requiredHeight = child.height;

  while (parent && parent.type !== "SECTION" && parent.type !== "PAGE") {
    if (typeof parent.resizeWithoutConstraints !== "function") return;

    const horizontalInset = Math.max(0, child.x);
    const verticalInset = Math.max(0, child.y);
    const targetWidth = Math.ceil(child.x + requiredWidth + horizontalInset);
    const targetHeight = Math.ceil(child.y + requiredHeight + verticalInset);
    const nextWidth = Math.max(parent.width, targetWidth);
    const nextHeight = Math.max(parent.height, targetHeight);

    if (nextWidth > parent.width || nextHeight > parent.height) {
      parent.resizeWithoutConstraints(nextWidth, nextHeight);
      mutatedNodeIds.push(parent.id);
    }

    if (parent.type === "INSTANCE" && parent.name === ".tree node") return;

    requiredWidth = nextWidth;
    requiredHeight = nextHeight;
    child = parent;
    parent = parent.parent;
  }
}

function hideUnusedInstances(instances, mutatedNodeIds) {
  for (const instance of instances) {
    hideInstance(instance, mutatedNodeIds);
  }
}

function hideInstance(instance, mutatedNodeIds) {
  if (instance.visible === false) return;

  instance.visible = false;
  mutatedNodeIds.push(instance.id);
}

function findUsageChipProperty(moduleInstance, propertyName, propertyType) {
  const properties = (moduleInstance.componentProperties || {}) as Record<string, { type: string }>;
  const propertyEntry = Object.entries(properties)
    .find(([key, property]) => (key === propertyName || key.startsWith(`${propertyName}#`)) && property.type === propertyType);

  return propertyEntry?.[0];
}

function usageChipsForConventionPlugins(providedByConventionPlugins) {
  return sortedUnique(
    providedByConventionPlugins.map((usage) => usage.pluginId)
  ).map((pluginId) => ({
    kind: USAGE_CHIP_KINDS.conventionPlugin,
    name: pluginId,
  }));
}

const MODULE_HORIZONTAL_PADDING = 26;
const MODULE_VERTICAL_PADDING = 6;
