package com.marmatsan.dependencies.tree.plugin

import com.marmatsan.dependencies.tree.model.DependencyNode
import com.marmatsan.dependencies.tree.node.Node

fun pluginTree(
    rootId: String,
    content: PluginScope.() -> Unit
): Node<DependencyNode.Plugin> {
    val root = Node(DependencyNode.Plugin(rootId))
    val builder = PluginScope(root)
    builder.content()
    return root
}