package com.marmatsan.dependencies.tree.node

import com.marmatsan.dependencies.tree.model.DependencyNode

data class Node<T : DependencyNode>(
    val data: T,
    var children: MutableList<Node<T>> = mutableListOf()
) {
    fun add(
        child: Node<T>
    ) = children.add(child)

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

        children.forEach { child ->
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