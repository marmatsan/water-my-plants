package com.marmatsan.dependencies.tree.dsl.plugin

import com.marmatsan.dependencies.tree.model.DependencyNode
import com.marmatsan.dependencies.tree.node.Node

fun pluginTree(
    rootId: String,
    content: PluginScope.() -> Unit
): Node<DependencyNode.Plugin> {
    val root = Node(
        DependencyNode.Plugin(
            pluginId = rootId
        )
    )
    val scope = PluginScope(
        root = root
    )
    scope.content()
    return root
}
