package com.marmatsan.dependencies.tree

import com.marmatsan.dependencies.tree.model.DependencyNode
import com.marmatsan.dependencies.tree.node.Node

/**
 * Base state for dependency-tree DSL scopes.
 *
 * [currentParent] identifies the node that receives the next relative declaration. Concrete DSL
 * scopes control parent changes so consumers cannot mutate traversal state directly.
 *
 * @param T Dependency payload stored in the tree.
 * @param root Initial parent for declarations made by the concrete scope.
 */
open class TreeBuilder<T : DependencyNode>(
    root: Node<T>
) {
    /** Node that receives the next relative declaration from a concrete DSL scope. */
    protected var currentParent: Node<T> = root
}
