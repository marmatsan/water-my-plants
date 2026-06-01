package com.marmatsan.dependencies.tree.dsl.library

import com.marmatsan.dependencies.tree.model.DependencyNode
import com.marmatsan.dependencies.tree.node.Node

fun libraryTree(
    rootGroup: String,
    content: LibraryScope.() -> Unit
): Node<DependencyNode.Library> {
    val root = Node(DependencyNode.Library(rootGroup))
    val scope = LibraryScope(root)
    content.invoke(scope)
    return root
}
