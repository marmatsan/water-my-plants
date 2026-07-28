package com.marmatsan.dependencies.gradle.tree.mapping

import com.marmatsan.dependencies.catalog.DependencyCatalogTrees
import com.marmatsan.dependencies.catalog.api.DependencyCatalog
import com.marmatsan.dependencies.catalog.api.LibraryCatalogEntry
import com.marmatsan.dependencies.catalog.api.LibraryCatalogNode
import com.marmatsan.dependencies.catalog.api.PluginCatalogNode
import com.marmatsan.dependencies.tree.model.DependencyNode
import com.marmatsan.dependencies.tree.model.LibraryEntry
import com.marmatsan.dependencies.tree.node.Node

/** Maps reusable tree DSL nodes to the stable dependency catalog API. */
internal class DependencyCatalogTreeApiMapper {
    /** Returns the immutable API catalog represented by [trees]. */
    fun map(
        trees: DependencyCatalogTrees,
    ): DependencyCatalog =
        DependencyCatalog(
            libraries = trees.libraries.map(::libraryNode),
            plugins = trees.plugins.map(::pluginNode),
        )

    private fun libraryNode(
        node: Node<DependencyNode.Library>,
    ): LibraryCatalogNode =
        LibraryCatalogNode(
            group = node.value.libraryGroup,
            entries =
                node.value.entries
                    .orEmpty()
                    .map(::libraryEntry),
            children = node.children.map(::libraryNode),
        )

    private fun libraryEntry(
        entry: LibraryEntry,
    ): LibraryCatalogEntry =
        when (entry) {
            is LibraryEntry.Single -> {
                LibraryCatalogEntry.Artifact(
                    name = entry.artifact.artifact,
                    version = entry.artifact.version,
                )
            }

            is LibraryEntry.Bundle -> {
                LibraryCatalogEntry.Bundle(
                    alias = entry.artifactsBundle.alias,
                    artifacts = entry.artifactsBundle.artifacts.map { artifact -> artifact.artifact },
                    version = entry.artifactsBundle.version,
                )
            }
        }

    private fun pluginNode(
        node: Node<DependencyNode.Plugin>,
    ): PluginCatalogNode =
        PluginCatalogNode(
            id = node.value.pluginId,
            version = node.value.version,
            children = node.children.map(::pluginNode),
        )
}
