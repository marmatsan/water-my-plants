import {
  ARTIFACT_INSTANCE_NAME,
  ARTIFACTS_BUNDLE_INSTANCE_NAME,
  ARTIFACTS_BUNDLE_PROPS,
  ARTIFACT_PROPS,
  MODULE_INSTANCE_NAME,
  MODULE_PROPS,
  SMALL_MODULE_SIZE,
} from "../config/figma-config";
import { updateNamedTextNodes } from "./figma-text-gateway";

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
    artifactInstance.setProperties({
      [ARTIFACT_PROPS.showConsumerModules]: artifact.requiredByModules.length > 0,
    });
    mutatedNodeIds.push(artifactInstance.id);
    await updateConsumerModuleInstances(
      artifactInstance,
      "Required by",
      artifact.requiredByModules,
      mutatedNodeIds
    );
  }
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
    bundleInstance.setProperties({
      [ARTIFACTS_BUNDLE_PROPS.showConsumerModules]: bundle.requiredByModules.length > 0,
    });
    mutatedNodeIds.push(bundleInstance.id);
    await updateConsumerModuleInstances(
      bundleInstance,
      "Required by",
      bundle.requiredByModules,
      mutatedNodeIds,
      { excludeArtifactDescendants: true }
    );
  }
}

export async function updateConsumerModuleInstances(
  root,
  heading,
  modules,
  mutatedNodeIds,
  options: ConsumerModuleOptions = {}
) {
  if (modules.length > 0) {
    requireConsumerModuleHeading(root, heading);
  }

  const moduleInstances = root.findAllWithCriteria({ types: ["INSTANCE"] })
    .filter((candidate) => candidate.name === MODULE_INSTANCE_NAME)
    .filter((candidate) => !options.excludeArtifactDescendants || !hasAncestorInstanceNamed(candidate, ARTIFACT_INSTANCE_NAME, root));

  if (moduleInstances.length < modules.length) {
    throw new Error(
      `Node '${root.id}' expected at least ${modules.length} '${MODULE_INSTANCE_NAME}' instances for '${heading}', ` +
        `found ${moduleInstances.length}. Update the .tree node component structure before writing metadata.`
    );
  }

  for (let index = 0; index < modules.length; index += 1) {
    const moduleInstance = moduleInstances[index];
    await setModuleInstance(moduleInstance, modules[index], mutatedNodeIds);
  }

  for (const moduleInstance of moduleInstances.slice(modules.length)) {
    if (moduleInstance.visible === false) continue;

    moduleInstance.visible = false;
    mutatedNodeIds.push(moduleInstance.id);
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

function requireConsumerModuleHeading(root, heading) {
  const hasHeading = root.findAllWithCriteria({ types: ["TEXT"] })
    .some((textNode) => textNode.name === "label" && textNode.characters === heading);

  if (!hasHeading) {
    throw new Error(`Node '${root.id}' is missing '${heading}' consumer module heading text.`);
  }
}

async function setModuleInstance(moduleInstance, moduleName, mutatedNodeIds) {
  requireModuleVariantProperty(moduleInstance, MODULE_PROPS.name);

  moduleInstance.visible = true;
  const properties = moduleInstance.componentProperties?.[MODULE_PROPS.size]
    ? {
        [MODULE_PROPS.name]: moduleName,
        [MODULE_PROPS.size]: SMALL_MODULE_SIZE,
      }
    : {
        [MODULE_PROPS.name]: moduleName,
      };

  moduleInstance.setProperties(properties);
  mutatedNodeIds.push(moduleInstance.id);
  await updateNamedTextNodes(moduleInstance, "label", [moduleName], mutatedNodeIds);
}

function requireModuleVariantProperty(moduleInstance, propertyName) {
  const property = moduleInstance.componentProperties?.[propertyName];
  if (!property || property.type !== "VARIANT") {
    throw new Error(
      `Expected '${moduleInstance.id}' to be a '${MODULE_INSTANCE_NAME}' instance with '${propertyName}' variant property.`
    );
  }
}
