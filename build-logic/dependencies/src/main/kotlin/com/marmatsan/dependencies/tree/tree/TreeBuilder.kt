package com.marmatsan.dependencies.tree.tree

import com.marmatsan.dependencies.tree.model.NodeData

open class TreeBuilder<T : NodeData>(
    root: TreeNode<T>
) {
    protected var currentParent: TreeNode<T> = root
}