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
     * [id] becomes the first part of every descendant Gradle plugin id and catalog alias. It must
     * be exactly one segment such as `com` or `org`; compact dotted paths belong in
     * [PluginScope.plugin] declarations below this root. A non-null [version] registers a
     * single-segment plugin directly at the root.
     *
     * @param id Exact top-level Gradle plugin id value.
     * @param version Optional version when the root itself is a registered plugin.
     * @param content Plugin declarations below the root.
     * @throws IllegalArgumentException if [id] is not exactly one path segment or the same root
     * was already declared in this catalog.
     */
    fun root(
        id: String,
        version: String? = null,
        content: PluginScope.() -> Unit = {},
    ) {
        require(roots.none { root -> root.value.pluginId == id }) {
            "Plugin catalog root '$id' is already declared"
        }
        roots +=
            pluginTree(
                rootId = id,
                version = version,
                content = content,
            )
    }

    internal fun values(): List<Node<DependencyNode.Plugin>> = roots.toList()
}
