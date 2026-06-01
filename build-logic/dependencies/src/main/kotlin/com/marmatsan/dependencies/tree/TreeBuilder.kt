package com.marmatsan.dependencies.tree

import com.marmatsan.dependencies.tree.model.DependencyNode
import com.marmatsan.dependencies.tree.node.Node

open class TreeBuilder<T : DependencyNode>(
    root: Node<T>
) {
    protected var currentParent: Node<T> = root
}