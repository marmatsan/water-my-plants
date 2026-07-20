package com.marmatsan.dependencies.catalog

import com.marmatsan.dependencies.tree.model.DependencyNode
import com.marmatsan.dependencies.tree.node.Node

data class DependencyCatalogTrees(
    val libraries: List<Node<DependencyNode.Library>>,
    val plugins: List<Node<DependencyNode.Plugin>>,
)
