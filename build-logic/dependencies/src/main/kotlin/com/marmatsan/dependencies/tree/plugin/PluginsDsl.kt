package com.marmatsan.dependencies.tree.plugin

import com.marmatsan.dependencies.tree.model.NodeData
import com.marmatsan.dependencies.tree.tree.TreeNode

fun pluginTree(
    rootId: String,
    content: PluginScope.() -> Unit
): TreeNode<NodeData.Plugin> {
    val root = TreeNode(NodeData.Plugin(rootId))
    val builder = PluginScope(root)
    builder.content()
    return root
}