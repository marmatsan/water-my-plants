package com.marmatsan.figmaDesignSync.domain.model.catalog

/**
 * Root container for the plugin catalog tree rendered into Figma.
 *
 * Plugin trees can come from `build-logic/settings.gradle.kts`,
 * `build-logic/dependencies/src/main/kotlin/com/marmatsan/dependencies/PluginTrees.kt`,
 * custom convention plugins, and plugins applied by project modules.
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
