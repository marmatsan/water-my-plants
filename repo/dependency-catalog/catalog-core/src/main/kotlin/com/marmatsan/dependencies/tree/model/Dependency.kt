package com.marmatsan.dependencies.tree.model

/**
 * Represents a final dependency emitted from a dependency tree.
 *
 * `Dependency` values are produced after traversing [DependencyNode] trees and resolving each
 * included node to its full dotted path. They are the domain objects consumed by the version catalog
 * registration layer.
 *
 * - [Library] represents a Maven group plus one or more artifact entries.
 * - [Plugin] represents a Gradle plugin id plus its version.
 */
sealed class Dependency {
    /**
     * Represents a library dependency group ready to be registered in a version catalog.
     *
     * [libraryGroup] is usually the full group built from the tree path, such as
     * `androidx.compose.ui`. [entries] contains the artifacts or bundles declared under that group.
     *
     * @property libraryGroup Full Maven group identifier.
     * @property entries Entries to register for this group. A `null` value means the node did not
     * declare any catalog entries and should normally not be emitted.
     */
    data class Library(
        val libraryGroup: String,
        val entries: List<LibraryEntry>? = null
    ) : Dependency()

    /**
     * Represents a Gradle plugin ready to be registered in a version catalog.
     *
     * @property pluginId Full Gradle plugin id, such as `org.jetbrains.kotlin.android`.
     * @property version Plugin version.
     */
    data class Plugin(
        val pluginId: String,
        val version: String
    ) : Dependency()
}
