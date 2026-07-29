package com.marmatsan.dependencies.gradle.tree.dsl

/**
 * Compatibility alias for the catalog-core plugin tree scope.
 *
 * New provider implementations should import
 * [com.marmatsan.dependencies.catalog.dsl.PluginCatalogTreesScope] directly.
 */
@Deprecated(
    message = "Use com.marmatsan.dependencies.catalog.dsl.PluginCatalogTreesScope",
    replaceWith = ReplaceWith("com.marmatsan.dependencies.catalog.dsl.PluginCatalogTreesScope")
)
typealias PluginCatalogTreesScope =
    com.marmatsan.dependencies.catalog.dsl.PluginCatalogTreesScope
