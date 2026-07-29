package com.marmatsan.figmaDocumentationSync.domain.model.catalog

/**
 * Root container for the plugin catalog tree rendered into Figma.
 *
 * Plugin trees can come from the dependency catalog provider selected by the
 * host project, configured included-build settings files, custom convention
 * plugins, and plugins applied by project modules.
 *
 * Example:
 * ```
 * PluginCatalogTree(
 *     roots = listOf(PluginCatalogNode(id = "com"))
 * )
 * ```
 *
 * @property roots Top-level plugin-id nodes.
 */
data class PluginCatalogTree(
    val roots: List<PluginCatalogNode>
)
