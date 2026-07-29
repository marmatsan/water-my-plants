import {
  ARTIFACT_INSTANCE_NAME,
  ARTIFACTS_BUNDLE_INSTANCE_NAME,
} from "@figma-documentation-sync/project-config";
import {
  libraryArtifacts,
  libraryBundles,
} from "../domain/catalog/library-catalog-entries";
import type { FlattenedCatalogNode } from "../domain/design-model";
import { directCatalogItemInstances } from "./figma-consumer-modules-gateway";

/**
 * Reports whether an existing Figma library tree node has enough structural
 * slots to represent every direct artifact, bundle, and artifact inside each
 * bundle from the generated catalog node.
 */
export function canRepresentLibraryCatalogEntries(
  instance,
  node: FlattenedCatalogNode
) {
  const artifacts = libraryArtifacts(node.entries);
  const bundles = libraryBundles(node.entries);
  const artifactInstances = directCatalogItemInstances(instance, ARTIFACT_INSTANCE_NAME);
  const bundleInstances = directCatalogItemInstances(instance, ARTIFACTS_BUNDLE_INSTANCE_NAME);

  if (artifactInstances.length < artifacts.length || bundleInstances.length < bundles.length) {
    return false;
  }

  return bundles.every((bundle, index) =>
    directCatalogItemInstances(bundleInstances[index], ARTIFACT_INSTANCE_NAME).length >= bundle.artifacts.length
  );
}

/**
 * Reports whether the tree node has enough interchangeable direct catalog-item
 * slots, regardless of the component currently selected in each slot.
 */
export function hasLibraryCatalogItemSlotCapacity(
  instance,
  node: FlattenedCatalogNode
) {
  return withInvisibleInstanceChildren(() => {
    const requiredSlots = libraryArtifacts(node.entries).length + libraryBundles(node.entries).length;
    return directLibraryCatalogItemSlots(instance).length >= requiredSlots;
  });
}

/**
 * Selects the artifact or bundle component required by each direct catalog
 * item. Returns false when the existing node cannot be reshaped safely.
 */
export async function configureLibraryCatalogItemSlots(
  section,
  instance,
  node: FlattenedCatalogNode,
  mutatedNodeIds
) {
  return withInvisibleInstanceChildren(async () => {
    if (!hasLibraryCatalogItemSlotCapacity(instance, node)) return false;

    const artifacts = libraryArtifacts(node.entries);
    const bundles = libraryBundles(node.entries);
    const slots = directLibraryCatalogItemSlots(instance);
    const artifactComponent = artifacts.length > 0
      ? await findCatalogItemComponent(section, ARTIFACT_INSTANCE_NAME)
      : null;
    const requiredNestedArtifactSlots = Math.max(0, ...bundles.map((bundle) => bundle.artifacts.length));
    const bundleComponent = bundles.length > 0
      ? await findCatalogItemComponent(
          section,
          ARTIFACTS_BUNDLE_INSTANCE_NAME,
          requiredNestedArtifactSlots
        )
      : null;

    if ((artifacts.length > 0 && !artifactComponent) || (bundles.length > 0 && !bundleComponent)) {
      return false;
    }

    const desiredSlots = [
      ...artifacts.map(() => ({ name: ARTIFACT_INSTANCE_NAME, component: artifactComponent })),
      ...bundles.map((bundle) => ({
        name: ARTIFACTS_BUNDLE_INSTANCE_NAME,
        component: bundleComponent,
        nestedArtifactCount: bundle.artifacts.length,
      })),
    ];

    for (let index = 0; index < desiredSlots.length; index += 1) {
      const slot = slots[index];
      const desired = desiredSlots[index];
      const currentComponent = await slot.getMainComponentAsync();
      let mutated = false;
      if (currentComponent?.id !== desired.component.id) {
        slot.swapComponent(desired.component);
        mutated = true;
      }
      if (slot.name !== desired.name) {
        slot.name = desired.name;
        mutated = true;
      }
      if (mutated) mutatedNodeIds.push(slot.id);

      if (desired.name === ARTIFACTS_BUNDLE_INSTANCE_NAME) {
        const nestedSlotsReady = configureBundleArtifactSlotVisibility(
          slot,
          desired.nestedArtifactCount,
          mutatedNodeIds
        );
        if (!nestedSlotsReady) return false;
      }
    }

    return canRepresentLibraryCatalogEntries(instance, node);
  });
}

export function directLibraryCatalogItemSlots(root) {
  const container = childrenOf(root)
    .find((child) => child.name === "artifacts" && "children" in child);
  const directSlots = childrenOf(container || root)
    .filter((candidate) =>
      candidate.type === "INSTANCE" &&
        (candidate.name === ARTIFACT_INSTANCE_NAME || candidate.name === ARTIFACTS_BUNDLE_INSTANCE_NAME)
    );

  if (directSlots.length > 0) return directSlots;

  return root.findAllWithCriteria({ types: ["INSTANCE"] })
    .filter((candidate) =>
      candidate.name === ARTIFACT_INSTANCE_NAME || candidate.name === ARTIFACTS_BUNDLE_INSTANCE_NAME
    )
    .filter((candidate) =>
      candidate.name !== ARTIFACT_INSTANCE_NAME ||
        !hasAncestorInstanceNamed(candidate, ARTIFACTS_BUNDLE_INSTANCE_NAME, root)
    );
}

async function findCatalogItemComponent(section, instanceName, requiredNestedArtifactSlots = 0) {
  const candidates = section.findAllWithCriteria({ types: ["INSTANCE"] })
    .filter((candidate) => candidate.name === instanceName)
    .filter((candidate) =>
      instanceName !== ARTIFACTS_BUNDLE_INSTANCE_NAME ||
        directCatalogItemInstances(candidate, ARTIFACT_INSTANCE_NAME).length >= requiredNestedArtifactSlots
    );

  for (const candidate of candidates) {
    const component = await candidate.getMainComponentAsync();
    if (component) return component;
  }

  return null;
}

function configureBundleArtifactSlotVisibility(bundleInstance, requiredSlots, mutatedNodeIds) {
  const slots = directCatalogItemInstances(bundleInstance, ARTIFACT_INSTANCE_NAME);
  if (slots.length < requiredSlots) return false;

  for (let index = 0; index < slots.length; index += 1) {
    const visible = index < requiredSlots;
    if (slots[index].visible === visible) continue;

    slots[index].visible = visible;
    mutatedNodeIds.push(slots[index].id);
  }

  return true;
}

function withInvisibleInstanceChildren(operation) {
  if (typeof figma === "undefined") return operation();

  const previousValue = figma.skipInvisibleInstanceChildren;
  figma.skipInvisibleInstanceChildren = false;
  try {
    const result = operation();
    if (result && typeof result.then === "function") {
      return result.finally(() => {
        figma.skipInvisibleInstanceChildren = previousValue;
      });
    }
    figma.skipInvisibleInstanceChildren = previousValue;
    return result;
  } catch (error) {
    figma.skipInvisibleInstanceChildren = previousValue;
    throw error;
  }
}

function childrenOf(node) {
  return "children" in node ? [...node.children] : [];
}

function hasAncestorInstanceNamed(node, name, boundary) {
  let current = node.parent;
  while (current && current.id !== boundary.id) {
    if (current.type === "INSTANCE" && current.name === name) return true;
    current = current.parent;
  }
  return false;
}
