package com.marmatsan.dependencies.catalog.dsl

import com.marmatsan.dependencies.tree.dsl.library.LibraryScope
import com.marmatsan.dependencies.tree.dsl.library.libraryTree
import com.marmatsan.dependencies.tree.model.DependencyNode
import com.marmatsan.dependencies.tree.node.Node

/**
 * Collects library trees declared for one dependency catalog.
 *
 * Every [root] must be unique. Nested paths are configured through [LibraryScope], including its
 * compact dot-separated path syntax.
 */
class LibraryCatalogTreesScope internal constructor() {
    private val roots = mutableListOf<Node<DependencyNode.Library>>()

    /**
     * Adds one Maven group root and configures its dependency subtree.
     *
     * [group] becomes the first part of every descendant Maven group and generated catalog alias.
     * It must be exactly one segment such as `com`, `io`, or `org`; compact dotted paths belong in
     * [LibraryScope.library] declarations below this root.
     *
     * @param group Exact top-level Maven group value.
     * @param content Library declarations below the root.
     * @throws IllegalArgumentException if [group] is not exactly one path segment or the same root
     * was already declared in this catalog.
     */
    fun root(
        group: String,
        content: LibraryScope.() -> Unit
    ) {
        require(roots.none { root -> root.value.libraryGroup == group }) {
            "Library catalog root '$group' is already declared"
        }
        roots +=
            libraryTree(
                rootGroup = group,
                content = content
            )
    }

    internal fun values(): List<Node<DependencyNode.Library>> = roots.toList()
}
