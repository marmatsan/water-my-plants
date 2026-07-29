package com.marmatsan.dependencies.tree.node

import com.marmatsan.dependencies.tree.model.DependencyNode

/**
 * A generic tree node used to model hierarchical dependency declarations.
 *
 * Each node stores a [DependencyNode] payload and zero or more children of the same payload type.
 * The tree shape is used to build dotted dependency paths, such as `androidx.compose.ui` or
 * `org.jetbrains.kotlin.android`, while the node payload decides whether a node should be emitted
 * as a final dependency.
 *
 * @param T The dependency node payload type stored in this tree.
 * @property value The payload associated with this node.
 * @property children The mutable list of child nodes declared below this node.
 */
data class Node<T : DependencyNode>(
    val value: T,
    var children: MutableList<Node<T>> = mutableListOf()
) {
    /**
     * Appends [child] to this node's children preserving declaration order.
     */
    fun add(
        child: Node<T>
    ) = children.add(
        element = child
    )

    /**
     * Traverses this tree in depth-first pre-order and maps matching nodes to results.
     *
     * The traversal visits the current node before its children. For every visited node, [pathSegment]
     * provides the segment that is appended to the current path. When [shouldIncludeNode] returns `true`,
     * the current path is joined with dots and passed to [mapNode] together with the node payload.
     *
     * This function does not require included nodes to be structural leaves. A node can be included in
     * the result and still have children.
     *
     * For example, given this library tree:
     *
     * ```
     * androidx
     * |-- activity        entries = [activity-compose]
     * `-- compose         entries = [compose-bom]
     *     |-- ui          entries = [ui, ui-graphics]
     *     `-- material3   entries = [material3]
     * ```
     *
     * A caller can flatten the tree into dependency-like results with:
     *
     * ```
     * root.depthFirstPreOrderTraverse(
     *     pathSegment = { libraryNode -> libraryNode.libraryGroup },
     *     shouldIncludeNode = { libraryNode -> libraryNode.entries != null },
     *     mapNode = { libraryNode, fullPath ->
     *         libraryNode.copy(libraryGroup = fullPath)
     *     }
     * )
     * ```
     *
     * The traversal visits nodes in this order:
     *
     * ```
     * androidx
     * androidx.activity
     * androidx.compose
     * androidx.compose.ui
     * androidx.compose.material3
     * ```
     *
     * Since `shouldIncludeNode` checks for non-null entries, the root `androidx` node is skipped and
     * the returned list contains the mapped nodes for:
     *
     * ```
     * androidx.activity
     * androidx.compose
     * androidx.compose.ui
     * androidx.compose.material3
     * ```
     *
     * @param pathSegment Returns the path segment contributed by a node payload.
     * @param shouldIncludeNode Decides whether the current node should be mapped into the returned list.
     * @param mapNode Maps an included node payload and its full dotted path to the output type [R].
     * @return The mapped results in depth-first pre-order.
     */
    fun <R> depthFirstPreOrderTraverse(
        pathSegment: (T) -> String,
        shouldIncludeNode: (T) -> Boolean,
        mapNode: (value: T, fullPath: String) -> R
    ): List<R> {
        val results = mutableListOf<R>()

        fun visit(
            node: Node<T>,
            path: List<String>
        ) {
            val currentPath = path + pathSegment(node.value)

            if (shouldIncludeNode(node.value)) {
                val fullPath = currentPath.joinToString(".")
                results.add(
                    element =
                        mapNode(
                            node.value,
                            fullPath
                        )
                )
            }

            node.children.forEach { child ->
                visit(
                    node = child,
                    path = currentPath
                )
            }
        }

        visit(
            node = this,
            path = emptyList()
        )

        return results
    }
}
