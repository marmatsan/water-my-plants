package com.marmatsan.dependencies.tree.model

/**
 * Represents the payload stored in a dependency tree node.
 *
 * `DependencyNode` is a sealed hierarchy covering the two supported node kinds:
 *
 * - [Library]: A library dependency group that contains one or more artifacts (either single artifacts
 *   or bundles of artifacts).
 * - [Plugin]: A Gradle plugin dependency.
 *
 * This type is intended to be used as the value of a tree node, while the tree structure models
 * relationships such as grouping, nesting, or ordering between nodes.
 */
sealed class DependencyNode {

    /**
     * Represents a library group (typically a Maven groupId) and its artifact entries.
     *
     * A [Library] node groups one or more [LibraryEntry] items under the same [libraryGroup]. Each entry may
     * define:
     * - a single artifact ([LibraryEntry.Single]), or
     * - a bundle of artifacts referenced by an alias ([LibraryEntry.Bundle]).
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
        val entries: List<LibraryEntry>? = null
    ) : DependencyNode()

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
    ) : DependencyNode()
}