package com.marmatsan.waterMyPlants.projectConfig.catalog

import com.marmatsan.dependencies.catalog.DependencyCatalogTrees
import com.marmatsan.dependencies.catalog.api.DependencyCatalog
import com.marmatsan.dependencies.catalog.api.DependencyCatalogProvider
import com.marmatsan.dependencies.catalog.api.LibraryCatalogEntry
import com.marmatsan.dependencies.catalog.api.LibraryCatalogNode
import com.marmatsan.dependencies.catalog.api.PluginCatalogNode
import com.marmatsan.dependencies.tree.model.DependencyNode
import com.marmatsan.dependencies.tree.model.LibraryEntry
import com.marmatsan.dependencies.tree.node.Node
import java.io.File

/** Water My Plants implementation of the portable dependency catalog contract. */
class WaterMyPlantsCatalogProvider : DependencyCatalogProvider {
    /** Returns the catalog with versions resolved from [rootDir]. */
    override fun resolved(
        rootDir: File
    ): DependencyCatalog =
        WaterMyPlantsCatalog
            .resolved(
                rootDir = rootDir
            ).toApi()

    /** Returns the catalog with symbolic aliases used to map entries to version properties. */
    override fun withVersionAliases(): DependencyCatalog =
        WaterMyPlantsCatalog.withVersionAliases().toApi()
}

private fun DependencyCatalogTrees.toApi(): DependencyCatalog =
    DependencyCatalog(
        libraries = libraries.map(Node<DependencyNode.Library>::toApi),
        plugins = plugins.map(Node<DependencyNode.Plugin>::toApi)
    )

private fun Node<DependencyNode.Library>.toApi(): LibraryCatalogNode =
    LibraryCatalogNode(
        group = value.libraryGroup,
        entries = value.entries.orEmpty().map(LibraryEntry::toApi),
        children = children.map(Node<DependencyNode.Library>::toApi)
    )

private fun LibraryEntry.toApi(): LibraryCatalogEntry =
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

private fun Node<DependencyNode.Plugin>.toApi(): PluginCatalogNode =
    PluginCatalogNode(
        id = value.pluginId,
        version = value.version,
        children = children.map(Node<DependencyNode.Plugin>::toApi)
    )
