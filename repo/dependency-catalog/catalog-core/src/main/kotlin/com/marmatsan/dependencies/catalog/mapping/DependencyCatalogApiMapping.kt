package com.marmatsan.dependencies.catalog.mapping

import com.marmatsan.dependencies.catalog.DependencyCatalogTrees
import com.marmatsan.dependencies.catalog.api.DependencyCatalog
import com.marmatsan.dependencies.catalog.api.LibraryCatalogEntry
import com.marmatsan.dependencies.catalog.api.LibraryCatalogNode
import com.marmatsan.dependencies.catalog.api.PluginCatalogNode
import com.marmatsan.dependencies.tree.model.DependencyNode
import com.marmatsan.dependencies.tree.model.LibraryEntry
import com.marmatsan.dependencies.tree.node.Node

/**
 * Maps these reusable catalog trees to the stable dependency-catalog API.
 *
 * The mapping preserves declaration order, structural namespace nodes, artifacts, bundles, and
 * the concrete or symbolic version representation selected while building the trees.
 *
 * @return Immutable public catalog represented by this tree set.
 */
fun DependencyCatalogTrees.toDependencyCatalog(): DependencyCatalog =
    DependencyCatalog(
        libraries = libraries.map(Node<DependencyNode.Library>::toDependencyCatalogNode),
        plugins = plugins.map(Node<DependencyNode.Plugin>::toDependencyCatalogNode)
    )

private fun Node<DependencyNode.Library>.toDependencyCatalogNode(): LibraryCatalogNode =
    LibraryCatalogNode(
        group = value.libraryGroup,
        entries = value.entries.orEmpty().map(LibraryEntry::toDependencyCatalogEntry),
        children = children.map(Node<DependencyNode.Library>::toDependencyCatalogNode)
    )

private fun LibraryEntry.toDependencyCatalogEntry(): LibraryCatalogEntry =
    when (this) {
        is LibraryEntry.Single -> {
            LibraryCatalogEntry.Artifact(
                name = artifact.artifact,
                version = artifact.version
            )
        }

        is LibraryEntry.Bundle -> {
            LibraryCatalogEntry.Bundle(
                alias = artifactsBundle.alias,
                artifacts = artifactsBundle.artifacts.map { artifact -> artifact.artifact },
                version = artifactsBundle.version
            )
        }
    }

private fun Node<DependencyNode.Plugin>.toDependencyCatalogNode(): PluginCatalogNode =
    PluginCatalogNode(
        id = value.pluginId,
        version = value.version,
        children = children.map(Node<DependencyNode.Plugin>::toDependencyCatalogNode)
    )
