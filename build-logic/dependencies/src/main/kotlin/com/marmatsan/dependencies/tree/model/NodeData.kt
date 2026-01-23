package com.marmatsan.dependencies.tree.model

/**
 * Represents the payload stored in a dependency tree node.
 *
 * `NodeData` is a sealed hierarchy covering the two supported node kinds:
 *
 * - [Library]: A library dependency group that contains one or more artifacts (either single artifacts
 *   or bundles of artifacts).
 * - [Plugin]: A Gradle plugin dependency.
 *
 * This type is intended to be used as the value of a tree node, while the tree structure models
 * relationships such as grouping, nesting, or ordering between nodes.
 */
sealed class NodeData {

    /**
     * Represents a library group (typically a Maven groupId) and its artifact entries.
     *
     * A [Library] node groups one or more [Entry] items under the same [libraryGroup]. Each entry may
     * define:
     * - a single artifact ([Entry.Single]), or
     * - a bundle of artifacts referenced by an alias ([Entry.Bundle]).
     *
     * Versions may be specified at the [Artifact] level and/or at the [ArtifactsBundle] level.
     *
     * @property libraryGroup The group identifier for the library, usually matching Maven `groupId`.
     * @property entries Optional list of entries (single artifacts or bundles) belonging to this group.
     * If `null`, it can represent “no entries provided” or “entries not loaded”, depending on the
     * calling context.
     */
    data class Library(
        val libraryGroup: String,
        val entries: List<Entry>? = null
    ) : NodeData() {

        /**
         * Represents a single Maven artifact within a library group.
         *
         * @property artifact The artifact identifier, usually matching Maven `artifactId`.
         * @property version Optional version for the artifact. When absent, version may be provided
         * by a containing [ArtifactsBundle] or an external resolution mechanism.
         */
        data class Artifact(
            val artifact: String,
            val version: String? = null
        )

        /**
         * Represents a named collection of artifacts.
         *
         * Bundles are addressed by an [alias] and contain multiple [artifacts]. A bundle can optionally
         * provide a [version] that can act as a common/default version for the artifacts it contains,
         * depending on your resolution rules.
         *
         * @property alias Logical name used to reference this bundle.
         * @property artifacts The artifacts that belong to this bundle.
         * @property version Optional version shared by the bundle. Interpretation (default vs enforced)
         * depends on the resolution strategy.
         */
        data class ArtifactsBundle(
            val alias: String,
            val artifacts: List<Artifact>,
            val version: String? = null
        )

        /**
         * Represents an entry within a [Library] node.
         *
         * Entries are either:
         * - [Single]: a single [Artifact]
         * - [Bundle]: a bundle of artifacts ([ArtifactsBundle])
         */
        sealed interface Entry {

            /**
             * A library entry representing a single artifact.
             *
             * @property artifact The artifact definition.
             */
            data class Single(
                val artifact: Artifact
            ) : Entry

            /**
             * A library entry representing an artifacts bundle.
             *
             * @property artifactsBundle The bundle definition.
             */
            data class Bundle(
                val artifactsBundle: ArtifactsBundle
            ) : Entry
        }
    }

    /**
     * Represents a Gradle plugin dependency.
     *
     * @property pluginId The plugin id (e.g., `"com.android.application"`).
     * @property version Optional plugin version. When absent, version may be provided externally
     * (e.g., plugin management, version catalogs, or conventions).
     */
    data class Plugin(
        val pluginId: String,
        val version: String? = null
    ) : NodeData()
}