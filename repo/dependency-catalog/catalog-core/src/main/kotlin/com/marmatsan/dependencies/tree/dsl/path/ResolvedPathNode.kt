package com.marmatsan.dependencies.tree.dsl.path

import com.marmatsan.dependencies.tree.model.DependencyNode
import com.marmatsan.dependencies.tree.node.Node

/**
 * Identifies the terminal node resolved for a dependency path and its direct parent.
 *
 * The parent reference allows replacing the immutable node payload while preserving the terminal
 * node's children and declaration position.
 *
 * @param T Dependency payload stored by the resolved tree.
 * @property parent Direct parent of [node].
 * @property node Terminal path node.
 */
internal class ResolvedPathNode<T : DependencyNode>(
    private val parent: Node<T>,
    private val node: Node<T>,
) {
    /**
     * Replaces the terminal payload with [value] while preserving its children and sibling order.
     *
     * @return The replacement node now attached to the tree.
     */
    fun replaceValue(
        value: T,
    ): Node<T> {
        val nodeIndex =
            parent.children.indexOfFirst { child ->
                child === node
            }
        check(nodeIndex >= 0) {
            "Resolved dependency path node is no longer attached to its parent"
        }

        return Node(
            value = value,
            children = node.children,
        ).also { replacement ->
            parent.children[nodeIndex] = replacement
        }
    }

    /** Returns the current immutable payload of the terminal node. */
    fun value(): T = node.value

    /** Returns the terminal node so a node-specific DSL can configure its existing subtree. */
    fun terminalNode(): Node<T> = node
}
