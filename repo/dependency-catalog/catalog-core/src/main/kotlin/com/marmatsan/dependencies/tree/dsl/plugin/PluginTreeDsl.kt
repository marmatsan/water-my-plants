package com.marmatsan.dependencies.tree.dsl.plugin

import com.marmatsan.dependencies.tree.model.DependencyNode
import com.marmatsan.dependencies.tree.node.Node

/**
 * Creates one Gradle plugin dependency tree rooted at [rootId].
 *
 * Use this lower-level entry point when constructing catalog trees outside the Gradle settings
 * plugin. The root contributes the first plugin id path value; [content] declares descendants
 * relative to it. Set [version] when the root itself represents a versioned single-segment plugin.
 *
 * @param rootId Top-level Gradle plugin id value stored in the root node.
 * @param version Optional version assigned directly to the root plugin.
 * @param content Plugin tree declarations below the root.
 * @return Root node containing the configured plugin tree.
 * @throws IllegalArgumentException if [rootId] is not exactly one path segment.
 */
fun pluginTree(
    rootId: String,
    version: String? = null,
    content: PluginScope.() -> Unit = {}
): Node<DependencyNode.Plugin> {
    val root =
        Node(
            DependencyNode.Plugin(
                pluginId = rootId,
                version = version
            )
        )
    val scope =
        PluginScope(
            root = root
        )
    scope.content()
    return root
}
