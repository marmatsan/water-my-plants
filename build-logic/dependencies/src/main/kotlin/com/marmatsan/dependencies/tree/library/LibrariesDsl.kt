package com.marmatsan.dependencies.tree.library

import com.marmatsan.dependencies.tree.model.NodeData
import com.marmatsan.dependencies.tree.tree.TreeNode

fun libraryTree(
    rootGroup: String,
    content: LibraryScope.() -> Unit
): TreeNode<NodeData.Library> {
    val root = TreeNode(NodeData.Library(rootGroup))
    val scope = LibraryScope(root)
    content.invoke(scope)
    return root
}