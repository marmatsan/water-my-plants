package com.marmatsan.dependencies.tree.dsl.plugin

import com.marmatsan.dependencies.tree.model.DependencyNode
import com.marmatsan.dependencies.tree.TreeBuilder
import com.marmatsan.dependencies.tree.node.Node

class PluginScope(
    root: Node<DependencyNode.Plugin>
) : TreeBuilder<DependencyNode.Plugin>(root) {
    fun plugin(
        id: String,
        version: String? = null,
        content: PluginScope.() -> Unit = {}
    ) {
        val dependencyNode = DependencyNode.Plugin(
            id,
            version
        )
        val parent = currentParent
        val newNode = Node(dependencyNode)
        currentParent.add(newNode)
        currentParent = newNode
        content()
        currentParent = parent
    }
}
