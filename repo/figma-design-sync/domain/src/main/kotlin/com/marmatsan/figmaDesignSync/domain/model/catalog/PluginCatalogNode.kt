package com.marmatsan.figmaDesignSync.domain.model.catalog

/**
 * Node in the plugin catalog tree rendered in Figma.
 *
 * The [id] is one segment or full path of a Gradle plugin id. [version] is
 * present only where the catalog source declares it, and [appliedToModules]
 * records the modules that use the plugin so Figma can show usage context.
 *
 * Example:
 * ```
 * PluginCatalogNode(
 *     id = "com.android.application",
 *     version = CatalogVersion("9.2.1"),
 *     appliedToModules = listOf(":app")
 * )
 * ```
 *
 * @property id Plugin id segment or full plugin id represented by this node.
 * @property version Version metadata declared for the plugin, if any.
 * @property appliedToModules Sorted Gradle module paths applying this plugin.
 * @property children Nested plugin-id segments.
 */
data class PluginCatalogNode(
    val id: String,
    val version: CatalogVersion? = null,
    val appliedToModules: List<String> = emptyList(),
    val children: List<PluginCatalogNode> = emptyList()
)
