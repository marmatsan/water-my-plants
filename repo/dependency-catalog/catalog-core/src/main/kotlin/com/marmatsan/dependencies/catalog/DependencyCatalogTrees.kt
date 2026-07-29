package com.marmatsan.dependencies.catalog

import com.marmatsan.dependencies.tree.model.DependencyNode
import com.marmatsan.dependencies.tree.node.Node

/**
 * Complete library and plugin tree set ready to be mapped into version catalogs.
 *
 * @property libraries Independently rooted Maven group trees.
 * @property plugins Independently rooted Gradle plugin id trees.
 */
data class DependencyCatalogTrees(
    val libraries: List<Node<DependencyNode.Library>>,
    val plugins: List<Node<DependencyNode.Plugin>>
)
