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
