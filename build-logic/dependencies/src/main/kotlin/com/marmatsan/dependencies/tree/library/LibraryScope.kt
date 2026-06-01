package com.marmatsan.dependencies.tree.library

import com.marmatsan.dependencies.tree.model.DependencyNode
import com.marmatsan.dependencies.tree.TreeBuilder
import com.marmatsan.dependencies.tree.node.Node
import com.marmatsan.dependencies.tree.model.Artifact
import com.marmatsan.dependencies.tree.model.ArtifactsBundle
import com.marmatsan.dependencies.tree.model.LibraryEntry

class LibraryScope(
    root: Node<DependencyNode.Library>
) : TreeBuilder<DependencyNode.Library>(root) {

    private var entries: MutableList<LibraryEntry>? = null

    /**
     * Adds a single artifact entry to the current library.
     *
     * This represents a standalone dependency in the format `artifact:version`.
     *
     * If no [version] is provided, the version is expected to be managed externally (e.g., via a BOM).
     *
     * @param artifact The name of the artifact (e.g., `"activity-compose"`).
     * @param version The optional version of the artifact. If `null`, the artifact will be registered without a version.
     */
    fun artifact(
        artifact: String,
        version: String? = null
    ) {
        val newEntry = LibraryEntry.Single(
            artifact = Artifact(artifact, version)
        )
        entries = (entries ?: mutableListOf()).apply { add(newEntry) }
    }

    /**
     * Adds a bundle of artifacts that share the same version and are grouped under a common alias.
     *
     * This is useful for defining a logical group of related dependencies that can be referenced
     * together via the specified [alias], used when defining library bundles in a Gradle
     * [Version Catalog](https://docs.gradle.org/current/userguide/version_catalogs.html).
     *
     * Each artifact will be registered under the same [version]. If [version] is `null`, it is
     * expected that the version will be managed externally, (e.g., via a BOM).
     *
     * @param artifacts A vararg list of artifact names to include in the bundle (e.g., `"ui"`, `"ui-tooling"`).
     * @param alias The unique alias that identifies the bundle. This is used to reference all artifacts together so
     * it can be referenced via `libs.bundles.<alias>`.
     * @param version The shared version for all artifacts in the bundle. If `null`, the version will not be declared.
     */
    fun artifactsBundle(
        vararg artifacts: String,
        alias: String,
        version: String? = null
    ) {
        val newEntry = LibraryEntry.Bundle(
            artifactsBundle = ArtifactsBundle(
                alias = alias,
                artifacts = artifacts.map { Artifact(it, version) },
                version = version
            )
        )
        entries = (entries ?: mutableListOf()).apply { add(newEntry) }
    }

    /**
     * Defines and registers a nested library group within the current [LibraryScope] tree structure.
     *
     * This function allows building a hierarchical DSL-style declaration of library dependencies.
     * It creates a new [Node] representing a [DependencyNode.Library] group, applies the provided
     * [content] block (which may define individual artifacts or nested groups), and then inserts
     * the result into the parent's children list.
     *
     *
     * **Example**
     * ```
     * library("compose") {
     *     artifact("compose-bom", version = "2025.06.01")
     *     library("ui") {
     *         artifactsBundle(
     *             "ui", "ui-tooling", "ui-preview",
     *             alias = "composeUi", version = "1.6.0"
     *         )
     *     }
     * }
     * ```
     *
     * @param group The group name of the library (e.g., `"compose"` or `"lifecycle"`).
     * @param content A DSL block that configures the entries (artifacts or nested groups) for this library group.
     */
    fun library(
        group: String,
        content: (LibraryScope.() -> Unit)? = null
    ) {
        val node = Node(DependencyNode.Library(group))
        currentParent.add(node)

        val childScope = LibraryScope(node)
        content?.invoke(childScope)

        val updatedNodeValue = node.value.copy(
            entries = childScope.entries?.toList()
        )
        val updatedNode = Node(
            value = updatedNodeValue,
            children = node.children
        )

        val siblings = currentParent.children

        if (siblings.isNotEmpty()) {
            siblings[siblings.lastIndex] = updatedNode
        } else {
            siblings.add(updatedNode)
        }
    }
}