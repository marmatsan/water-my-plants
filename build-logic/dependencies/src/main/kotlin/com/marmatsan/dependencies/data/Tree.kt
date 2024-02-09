package com.marmatsan.dependencies.data

class TreeBuilder<T : NodeData>(root: TreeNode<T>) {
    private var currentParent = root

    fun tree(value: T, content: () -> Unit = {}) {
        val parent = currentParent
        val newNode = TreeNode(value)
        currentParent.add(newNode)
        currentParent = newNode
        content()
        currentParent = parent
    }
}

fun <T : NodeData> tree(rootData: T, content: TreeBuilder<T>.() -> Unit): TreeNode<T> {
    val root = TreeNode(rootData)
    val treeBuilder = TreeBuilder(root)
    treeBuilder.content()
    return root
}