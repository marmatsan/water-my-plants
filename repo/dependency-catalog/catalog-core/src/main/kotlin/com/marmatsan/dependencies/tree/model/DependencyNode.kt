package com.marmatsan.dependencies.tree.model

/**
 * Represents the payload stored in a dependency tree node.
 *
 * A `DependencyNode` may be an intermediate grouping node or a node that should be emitted as a
 * final [Dependency]. The tree traversal decides whether a node is included based on the payload:
 * library nodes are included when they have [Library.entries], and plugin nodes are included when
 * they have [Plugin.version].
 *
 * - [Library] contributes one segment to a Maven group path.
 * - [Plugin] contributes one segment to a Gradle plugin id path.
 */
sealed class DependencyNode {
    /**
     * Represents one segment in a library group tree.
     *
     * For intermediate nodes, [entries] is `null` and the node only contributes [libraryGroup] to
     * descendants' full paths. When [entries] is not `null`, the node is mapped to a
     * [Dependency.Library] using the full dotted path built by traversal.
     *
     * @property libraryGroup Path segment for this node.
     * @property entries Optional catalog entries declared at this node.
     */
    data class Library(
        val libraryGroup: String,
        val entries: List<LibraryEntry>? = null,
    ) : DependencyNode()

    /**
     * Represents one segment in a Gradle plugin id tree.
     *
     * For intermediate nodes, [version] is `null` and the node only contributes [pluginId] to
     * descendants' full plugin ids. When [version] is not `null`, the node is mapped to a
     * [Dependency.Plugin] using the full dotted path built by traversal.
     *
     * @property pluginId Path segment for this node.
     * @property version Optional plugin version declared at this node.
     */
    data class Plugin(
        val pluginId: String,
        val version: String? = null,
    ) : DependencyNode()
}
