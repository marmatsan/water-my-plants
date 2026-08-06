package com.marmatsan.projectConfig.figma.catalog

import com.marmatsan.figmaDocumentationSync.domain.model.catalog.CatalogVersion
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogEntry
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogNode
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogTree
import com.marmatsan.dependencies.catalog.api.LibraryCatalogEntry as SourceLibraryCatalogEntry
import com.marmatsan.dependencies.catalog.api.LibraryCatalogNode as SourceLibraryCatalogNode
import com.marmatsan.dependencies.catalog.api.PluginCatalogNode as SourcePluginCatalogNode

/** Pure mapper between the Dependency Catalog API and Figma catalog models. */
internal class DependencyCatalogTreeMapper : CatalogTreeMapper {
    /** Maps dependency-catalog library [roots] to a Figma domain tree. */
    override fun libraryTree(
        roots: List<SourceLibraryCatalogNode>
    ): LibraryCatalogTree =
        LibraryCatalogTree(
            roots = roots.map(::libraryNode)
        )

    /** Maps dependency-catalog plugin [roots] to a Figma domain tree. */
    override fun pluginTree(
        roots: List<SourcePluginCatalogNode>
    ): PluginCatalogTree =
        PluginCatalogTree(
            roots = roots.map(::pluginNode)
        )

    private fun libraryNode(
        source: SourceLibraryCatalogNode
    ): LibraryCatalogNode =
        LibraryCatalogNode(
            group = source.group,
            entries = source.entries.map(::libraryEntry),
            children = source.children.map(::libraryNode)
        )

    private fun libraryEntry(
        source: SourceLibraryCatalogEntry
    ): LibraryCatalogEntry =
        when (source) {
            is SourceLibraryCatalogEntry.Artifact -> {
                LibraryCatalogEntry.Artifact(
                    artifact = source.name,
                    version =
                        CatalogVersion(
                            value = source.version
                        )
                )
            }

            is SourceLibraryCatalogEntry.Bundle -> {
                LibraryCatalogEntry.ArtifactsBundle(
                    alias = source.alias,
                    artifacts = source.artifacts,
                    version =
                        CatalogVersion(
                            value = source.version
                        )
                )
            }
        }

    private fun pluginNode(
        source: SourcePluginCatalogNode
    ): PluginCatalogNode =
        PluginCatalogNode(
            id = source.id,
            version = source.version?.let(::CatalogVersion),
            children = source.children.map(::pluginNode)
        )
}
