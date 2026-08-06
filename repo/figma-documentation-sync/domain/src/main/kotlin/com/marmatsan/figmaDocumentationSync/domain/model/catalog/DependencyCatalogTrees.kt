package com.marmatsan.figmaDocumentationSync.domain.model.catalog

/**
 * Complete pair of library and plugin trees supplied by a configured catalog adapter.
 *
 * This value lets an adapter materialize catalog declarations before a Figma
 * task executes, without requiring the task to instantiate a product-specific
 * provider through reflection.
 *
 * @property libraries Library dependency tree with resolved usage metadata.
 * @property plugins Gradle plugin tree with resolved usage metadata.
 */
data class DependencyCatalogTrees(
    val libraries: LibraryCatalogTree,
    val plugins: PluginCatalogTree
)
