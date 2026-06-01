package com.marmatsan.dependencies.tree.node

import com.marmatsan.dependencies.tree.model.DependencyNode

data class Node<T : DependencyNode>(
    val value: T,
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
        mapNode: (value: T, fullPath: String) -> R
    ): List<R> {
        traversalPath.add(pathSegment(value))

        if (nodeIsLeaf(value)) {
            val fullPath = traversalPath.joinToString(".")
            results.add(mapNode(value, fullPath))
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