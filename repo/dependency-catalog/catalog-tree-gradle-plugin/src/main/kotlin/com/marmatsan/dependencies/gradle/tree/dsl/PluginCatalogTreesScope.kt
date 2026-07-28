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

    internal fun values(): List<Node<DependencyNode.Plugin>> = roots.toList()
}
