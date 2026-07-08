package com.marmatsan.figmaDesignSync.domain.model.catalog

/**
 * Domain entry that can be rendered under a library catalog node in Figma.
 *
 * Entries are produced from catalog sources such as
 * `repo/dependency-catalog/src/main/kotlin/com/marmatsan/dependencies/LibraryTrees.kt`
 * and configured included-build settings files, then serialized into
 * `design-model.json`.
 *
 * @sample com.marmatsan.figmaDesignSync.domain.samples.DomainKDocSamples.libraryCatalogEntrySample
 */
sealed interface LibraryCatalogEntry {
    /**
     * Gradle convention plugin that provides a dependency to production modules.
     *
     * @property pluginId Gradle plugin id applied by production modules.
     * @property pluginModule Gradle module path that implements the convention
     * plugin.
     * @property requiredByModules Sorted production module paths that receive
     * this dependency through the convention plugin.
     */
    data class ConventionPluginUsage(
        val pluginId: String,
        val pluginModule: String,
        val requiredByModules: List<String> = emptyList()
    )

    /**
     * Gradle convention plugin that uses a catalog artifact as build tooling
     * configuration instead of adding it to a production module dependency
     * bucket.
     *
     * @property pluginId Gradle plugin id implemented by the convention plugin.
     * @property pluginModule Gradle module path that implements the convention
     * plugin.
     * @property target Configuration target that consumes the artifact, such as
     * `protobuf.protoc.artifact`.
     */
    data class ConventionPluginConfigurationUsage(
        val pluginId: String,
        val pluginModule: String,
        val target: String
    )

    /**
     * Single Maven artifact declared in a version catalog or dependency DSL.
     *
     * @property artifact Maven artifact id without the group.
     * @property version Version metadata rendered with the artifact.
     * @property requiredByModules Sorted Gradle module paths that use this
     * artifact.
     * @property providedByConventionPlugins Gradle convention plugins that
     * provide this artifact to production modules.
     * @property configuredByConventionPlugins Gradle convention plugins that
     * use this artifact to configure tooling needed by the plugin.
     */
    data class Artifact(
        val artifact: String,
        val version: CatalogVersion,
        val requiredByModules: List<String> = emptyList(),
        val providedByConventionPlugins: List<ConventionPluginUsage> = emptyList(),
        val configuredByConventionPlugins: List<ConventionPluginConfigurationUsage> = emptyList()
    ) : LibraryCatalogEntry

    /**
     * Logical bundle that groups several Maven artifacts under one catalog alias.
     *
     * Bundles keep their alias because that is the stable label shown in the
     * generated Figma catalog.
     *
     * @property alias Catalog alias used as the bundle label.
     * @property artifacts Maven artifact ids grouped by this bundle.
     * @property version Version metadata shared by the bundle.
     * @property requiredByModules Sorted Gradle module paths that use this
     * bundle.
     * @property providedByConventionPlugins Gradle convention plugins that
     * provide this bundle to production modules.
     */
    data class ArtifactsBundle(
        val alias: String,
        val artifacts: List<String>,
        val version: CatalogVersion,
        val requiredByModules: List<String> = emptyList(),
        val providedByConventionPlugins: List<ConventionPluginUsage> = emptyList()
    ) : LibraryCatalogEntry
}
