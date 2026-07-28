package com.marmatsan.dependencies.tree.dsl.plugin

import com.marmatsan.dependencies.tree.TreeBuilder
import com.marmatsan.dependencies.tree.dsl.path.DependencyPath
import com.marmatsan.dependencies.tree.dsl.path.resolvePath
import com.marmatsan.dependencies.tree.model.DependencyNode
import com.marmatsan.dependencies.tree.node.Node

/**
 * Builds Gradle plugin nodes and relative plugin paths below [root].
 *
 * @param root Node whose subtree is configured by this scope.
 */
class PluginScope(
    root: Node<DependencyNode.Plugin>,
) : TreeBuilder<DependencyNode.Plugin>(root) {
    /**
     * Defines a plugin path relative to the current node.
     *
     * Dots in [id] delimit path segments, so `plugin("figma.code")` is equivalent to nesting
     * `plugin("figma") { plugin("code") { ... } }`. Existing prefixes are reused, [version] is
     * assigned to the terminal node, and [content] executes with that terminal node as its parent.
     * Only nodes with a version are registered; an unversioned node acts only as a namespace for
     * descendants. A registered plugin uses its complete dotted id as both Gradle plugin id and
     * catalog alias.
     *
     * @param id Relative plugin path.
     * @param version Optional version assigned to the terminal path node.
     * @param content Nested plugin declarations below the terminal path node.
     * @throws IllegalArgumentException if [id] is invalid or redefines a terminal node with a
     * different version.
     */
    fun plugin(
        id: String,
        version: String? = null,
        content: PluginScope.() -> Unit = {},
    ) {
        val resolvedNode =
            currentParent.resolvePath(
                path = DependencyPath.parse(id),
                segment = DependencyNode.Plugin::pluginId,
                createValue = { pathSegment ->
                    DependencyNode.Plugin(
                        pluginId = pathSegment,
                    )
                },
            )
        val existingVersion = resolvedNode.value().version
        require(version == null || existingVersion == null || version == existingVersion) {
            "Plugin path '$id' already declares version '$existingVersion' and cannot declare '$version'"
        }

        val parent = currentParent
        currentParent =
            resolvedNode.replaceValue(
                value =
                    resolvedNode.value().copy(
                        version = version ?: existingVersion,
                    ),
            )
        try {
            content()
        } finally {
            currentParent = parent
        }
    }
}
