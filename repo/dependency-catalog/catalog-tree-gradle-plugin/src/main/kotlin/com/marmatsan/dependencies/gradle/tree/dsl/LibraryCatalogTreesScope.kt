package com.marmatsan.dependencies.gradle.tree.dsl

import com.marmatsan.dependencies.tree.dsl.library.LibraryScope
import com.marmatsan.dependencies.tree.dsl.library.libraryTree
import com.marmatsan.dependencies.tree.model.Artifact
import com.marmatsan.dependencies.tree.model.DependencyNode
import com.marmatsan.dependencies.tree.model.LibraryEntry
import com.marmatsan.dependencies.tree.node.Node

/**
 * Collects library trees declared for one settings-owned version catalog.
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
     * The conventional form is one top-level segment such as `com`, `io`, or `org`.
     *
     * @param group Exact top-level Maven group value.
     * @param content Library declarations below the root.
     * @throws IllegalArgumentException if the same [group] was already declared in this catalog.
     */
    fun root(
        group: String,
        content: LibraryScope.() -> Unit,
    ) {
        require(roots.none { root -> root.value.libraryGroup == group }) {
            "Library catalog root '$group' is already declared"
        }
        roots +=
            libraryTree(
                rootGroup = group,
                content = content,
            )
    }

    /**
     * Adds one library from its complete Maven [group] and [artifact].
     *
     * This leaf-oriented shortcut is equivalent to declaring a [root], a relative `library(...)`
     * path, and its nested `artifact(...)`. Declarations sharing the same first group segment reuse
     * one root, so consumers can list sparse coordinates without manually building namespace
     * blocks. The generated alias follows the catalog API alias policy.
     *
     * Example:
     *
     * ```
     * libraries {
     *     library(
     *         group = "com.michael-bull.kotlin-result",
     *         artifact = "kotlin-result",
     *         version = version("kotlinResultLibraryVersion"),
     *     )
     * }
     * ```
     *
     * @param group Complete Maven group identifier.
     * @param artifact Maven artifact identifier.
     * @param version Concrete version, or `null` when supplied by a BOM or another constraint.
     * @throws IllegalArgumentException if [group] is not a valid dot-separated catalog path.
     */
    fun library(
        group: String,
        artifact: String,
        version: String? = null,
    ) {
        val segments = catalogPathSegments(group)
        val rootIndex =
            rootIndex(
                group = segments.first(),
            )
        val root =
            if (rootIndex >= 0) {
                roots[rootIndex]
            } else {
                Node(
                    value =
                        DependencyNode.Library(
                            libraryGroup = segments.first(),
                        ),
                ).also(roots::add)
            }

        if (segments.size == 1) {
            roots[roots.indexOf(root)] =
                root.copy(
                    value =
                        root.value.copy(
                            entries =
                                root.value.entries.orEmpty() +
                                    LibraryEntry.Single(
                                        artifact =
                                            Artifact(
                                                artifact = artifact,
                                                version = version,
                                            ),
                                    ),
                        ),
                )
        } else {
            LibraryScope(root).library(
                group = segments.drop(1).joinToString("."),
            ) {
                artifact(
                    artifact = artifact,
                    version = version,
                )
            }
        }
    }

    private fun rootIndex(
        group: String,
    ): Int =
        roots.indexOfFirst { root ->
            root.value.libraryGroup == group
        }

    internal fun values(): List<Node<DependencyNode.Library>> = roots.toList()
}
