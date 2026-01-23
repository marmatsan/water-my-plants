package com.marmatsan.dependencies.tree.tree

import com.marmatsan.dependencies.tree.model.NodeData

data class TreeNode<T : NodeData>(
    val data: T,
    var children: MutableList<TreeNode<T>> = mutableListOf()
) {
    fun add(child: TreeNode<T>) = children.add(child)

    fun <R> depthFirstPreOrderTraverse(
        traversalPath: MutableList<String> = mutableListOf(),
        results: MutableList<R> = mutableListOf(),
        pathSegment: (T) -> String,
        nodeIsLeaf: (T) -> Boolean,
        mapNode: (data: T, fullPath: String) -> R
    ): List<R> {
        traversalPath.add(pathSegment(data))

        if (nodeIsLeaf(data)) {
            val fullPath = traversalPath.joinToString(".")
            results.add(mapNode(data, fullPath))
        }

        children?.forEach { child ->
            child.depthFirstPreOrderTraverse(
                traversalPath = traversalPath,
                results = results,
                pathSegment = pathSegment,
                nodeIsLeaf = nodeIsLeaf,
                mapNode = mapNode
            )
        }

        traversalPath.removeLast()
        return results
    }
}