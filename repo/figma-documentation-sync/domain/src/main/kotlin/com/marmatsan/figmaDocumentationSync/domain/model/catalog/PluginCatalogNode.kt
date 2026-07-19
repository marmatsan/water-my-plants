package com.marmatsan.figmaDocumentationSync.domain.model.catalog

/**
 * Node in the plugin catalog tree rendered in Figma.
 *
 * The [id] is one segment or full path of a Gradle plugin id. [version] is
 * present only where the catalog source declares it, [appliedToModules]
 * records modules that apply the plugin directly, and
 * [providedByConventionPlugins] records convention plugins that apply it
 * indirectly when those convention plugins are used.
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
 * @property providedByConventionPlugins Gradle convention plugins that apply
 * this plugin for their consumers.
 * @property children Nested plugin-id segments.
 */
data class PluginCatalogNode(
    val id: String,
    val version: CatalogVersion? = null,
    val appliedToModules: List<String> = emptyList(),
    val providedByConventionPlugins: List<ConventionPluginUsage> = emptyList(),
    val children: List<PluginCatalogNode> = emptyList()
) {
    /**
     * Gradle convention plugin that applies this plugin for projects that apply
     * the convention plugin.
     *
     * @property pluginId Gradle plugin id applied by production modules.
     * @property pluginModule Gradle module path that implements the convention
     * plugin.
     * @property requiredByModules Sorted production module paths that currently
     * receive this plugin through the convention plugin. This list is empty
     * when no module applies the convention plugin yet.
     */
    data class ConventionPluginUsage(
        val pluginId: String,
        val pluginModule: String,
        val requiredByModules: List<String> = emptyList()
    )
}
