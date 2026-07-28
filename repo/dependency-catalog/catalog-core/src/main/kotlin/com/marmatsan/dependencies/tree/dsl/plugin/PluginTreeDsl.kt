package com.marmatsan.dependencies.tree.dsl.plugin

import com.marmatsan.dependencies.tree.model.DependencyNode
import com.marmatsan.dependencies.tree.node.Node

/**
 * Creates one Gradle plugin dependency tree rooted at [rootId].
 *
 * Use this lower-level entry point when constructing catalog trees outside the Gradle settings
 * plugin. The root contributes the first plugin id path value; [content] declares descendants
 * relative to it.
 *
 * @param rootId Top-level Gradle plugin id value stored in the root node.
 * @param content Plugin tree declarations below the root.
 * @return Root node containing the configured plugin tree.
 */
fun pluginTree(
    rootId: String,
    content: PluginScope.() -> Unit,
): Node<DependencyNode.Plugin> {
    val root =
        Node(
            DependencyNode.Plugin(
                pluginId = rootId,
            ),
        )
    val scope =
        PluginScope(
            root = root,
        )
    scope.content()
    return root
}
