package com.marmatsan.dependencies.tree.plugin

import com.marmatsan.dependencies.tree.model.NodeData
import com.marmatsan.dependencies.tree.tree.TreeBuilder
import com.marmatsan.dependencies.tree.tree.TreeNode

class PluginScope(root: TreeNode<NodeData.Plugin>) : TreeBuilder<NodeData.Plugin>(root) {
    fun plugin(
        id: String,
        version: String? = null,
        content: PluginScope.() -> Unit = {}
    ) {
        val nodeData = NodeData.Plugin(id, version)
        val parent = currentParent
        val newNode = TreeNode(nodeData)
        currentParent.add(newNode)
        currentParent = newNode
        content() // recursively build child plugins
        currentParent = parent
    }
}