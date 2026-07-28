package com.marmatsan.dependencies.gradle.tree.dsl

import com.marmatsan.dependencies.tree.dsl.plugin.PluginScope
import com.marmatsan.dependencies.tree.dsl.plugin.pluginTree
import com.marmatsan.dependencies.tree.model.DependencyNode
import com.marmatsan.dependencies.tree.node.Node

/**
 * Collects plugin trees declared for one settings-owned version catalog.
 *
 * Every [root] must be unique. Nested paths are configured through [PluginScope], including its
 * compact dot-separated path syntax.
 */
class PluginCatalogTreesScope internal constructor() {
    private val roots = mutableListOf<Node<DependencyNode.Plugin>>()

    /**
     * Adds one Gradle plugin id root and configures its dependency subtree.
     *
     * [id] becomes the first part of every descendant Gradle plugin id and catalog alias. The
     * conventional form is one top-level segment such as `com` or `org`.
     *
     * @param id Exact top-level Gradle plugin id value.
     * @param content Plugin declarations below the root.
     * @throws IllegalArgumentException if the same [id] was already declared in this catalog.
     */
    fun root(
        id: String,
        content: PluginScope.() -> Unit,
    ) {
        require(roots.none { root -> root.value.pluginId == id }) {
            "Plugin catalog root '$id' is already declared"
        }
        roots +=
            pluginTree(
                rootId = id,
                content = content,
            )
    }

    /**
     * Adds one plugin from its complete [id] and [version].
     *
     * This leaf-oriented shortcut is equivalent to declaring a [root] and nested relative
     * `plugin(...)` paths. Declarations sharing the same first id segment reuse one root, which
     * keeps sparse plugin catalogs flat while preserving the same hierarchical model and generated
     * type-safe accessor.
     *
     * Example:
     *
     * ```
     * plugins {
     *     plugin(
     *         id = "org.jetbrains.kotlin.jvm",
     *         version = version("kotlinVersion"),
     *     )
     * }
     * ```
     *
     * @param id Complete Gradle plugin id.
     * @param version Concrete plugin version.
     * @throws IllegalArgumentException if [id] is not a valid dot-separated catalog path or the
     * same plugin id already has a different version.
     */
    fun plugin(
        id: String,
        version: String,
    ) {
        val segments = catalogPathSegments(id)
        val rootIndex =
            rootIndex(
                id = segments.first(),
            )
        val root =
            if (rootIndex >= 0) {
                roots[rootIndex]
            } else {
                Node(
                    value =
                        DependencyNode.Plugin(
                            pluginId = segments.first(),
                        ),
                ).also(roots::add)
            }

        if (segments.size == 1) {
            val existingVersion = root.value.version
            require(existingVersion == null || existingVersion == version) {
                "Plugin path '$id' already declares version '$existingVersion' and cannot declare '$version'"
            }
            roots[roots.indexOf(root)] =
                root.copy(
                    value =
                        root.value.copy(
                            version = version,
                        ),
                )
        } else {
            PluginScope(root).plugin(
                id = segments.drop(1).joinToString("."),
                version = version,
            )
        }
    }

    private fun rootIndex(
        id: String,
    ): Int =
        roots.indexOfFirst { root ->
            root.value.pluginId == id
        }

    internal fun values(): List<Node<DependencyNode.Plugin>> = roots.toList()
}
