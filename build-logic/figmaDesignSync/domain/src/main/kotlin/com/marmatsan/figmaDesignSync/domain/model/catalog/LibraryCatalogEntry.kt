package com.marmatsan.figmaDesignSync.domain.model.catalog

/**
 * Domain entry that can be rendered under a library catalog node in Figma.
 *
 * Entries are produced from catalog sources such as
 * `build-logic/dependencies/src/main/kotlin/com/marmatsan/dependencies/LibraryTrees.kt`
 * and `build-logic/settings.gradle.kts`, then serialized into
 * `design-model.json`.
 *
 * @sample com.marmatsan.figmaDesignSync.domain.samples.DomainKDocSamples.libraryCatalogEntrySample
 */
sealed interface LibraryCatalogEntry {
    /**
     * Single Maven artifact declared in a version catalog or dependency DSL.
     *
     * @property artifact Maven artifact id without the group.
     * @property version Version metadata rendered with the artifact.
     * @property requiredByModules Sorted Gradle module paths that use this
     * artifact.
     */
    data class Artifact(
        val artifact: String,
        val version: CatalogVersion,
        val requiredByModules: List<String> = emptyList()
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
     */
    data class ArtifactsBundle(
        val alias: String,
        val artifacts: List<String>,
        val version: CatalogVersion,
        val requiredByModules: List<String> = emptyList()
    ) : LibraryCatalogEntry
}
