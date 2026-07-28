package com.marmatsan.dependencies.tree.dsl.path

import com.marmatsan.dependencies.tree.model.DependencyNode
import com.marmatsan.dependencies.tree.node.Node

/**
 * Resolves [path] below this node, reusing existing prefixes and creating missing nodes.
 *
 * @param path Relative path to resolve.
 * @param segment Returns the path segment represented by an existing payload.
 * @param createValue Creates the payload for a missing segment.
 * @return The resolved terminal node together with its direct parent.
 * @throws IllegalArgumentException when an existing tree contains duplicate matching siblings.
 */
internal fun <T : DependencyNode> Node<T>.resolvePath(
    path: DependencyPath,
    segment: (T) -> String,
    createValue: (String) -> T,
): ResolvedPathNode<T> {
    var parent = this
    var resolvedParent = this
    var resolvedNode: Node<T>? = null

    path.segments.forEach { pathSegment ->
        val matchingChildren =
            parent.children.filter { child ->
                segment(child.value) == pathSegment
            }
        require(matchingChildren.size <= 1) {
            "Dependency path segment '$pathSegment' is ambiguous below '${segment(parent.value)}'"
        }

        val child =
            matchingChildren.singleOrNull()
                ?: Node(
                    value = createValue(pathSegment),
                ).also(parent::add)

        resolvedParent = parent
        resolvedNode = child
        parent = child
    }

    return ResolvedPathNode(
        parent = resolvedParent,
        node = requireNotNull(resolvedNode),
    )
}
