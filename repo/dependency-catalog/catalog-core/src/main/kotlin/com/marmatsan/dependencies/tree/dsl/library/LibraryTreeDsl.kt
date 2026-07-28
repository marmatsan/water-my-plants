package com.marmatsan.dependencies.tree.dsl.library

import com.marmatsan.dependencies.tree.model.DependencyNode
import com.marmatsan.dependencies.tree.node.Node

/**
 * Creates one library dependency tree rooted at [rootGroup].
 *
 * Use this lower-level entry point when constructing catalog trees outside the Gradle settings
 * plugin. The root contributes the first Maven group path value; [content] declares descendants
 * and artifacts relative to it.
 *
 * @param rootGroup Top-level Maven group value stored in the root node.
 * @param content Library tree declarations below the root.
 * @return Root node containing the configured library tree.
 */
fun libraryTree(
    rootGroup: String,
    content: LibraryScope.() -> Unit,
): Node<DependencyNode.Library> {
    val root =
        Node(
            DependencyNode.Library(
                libraryGroup = rootGroup,
            ),
        )
    val scope =
        LibraryScope(
            root = root,
        )
    content.invoke(
        scope,
    )
    return root
}
