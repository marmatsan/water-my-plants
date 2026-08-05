package com.marmatsan.figmaDocumentationSync.plugin.generator.versions

import com.marmatsan.figmaDocumentationSync.domain.model.catalog.CatalogVersion
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogEntry
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogNode
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogTree
import com.marmatsan.figmaDocumentationSync.domain.model.versions.RepositoryVersionSection
import me.tatarka.inject.annotations.Inject

/**
 * Selects the repository versions referenced by visible production catalog
 * nodes.
 *
 * The selector keeps the configured section order, including empty sections,
 * so the Figma writer can remove stale instances from every visual target. A
 * catalog version contributes a key only when it is visible and its symbolic
 * value matches a property declared by the repository versions file.
 */
@Inject
internal class VisuallyReferencedVersionSectionsSelector {
    /**
     * Returns [sections] with entries not referenced by [libraryTree] or
     * [pluginTree] removed.
     */
    fun select(
        sections: List<RepositoryVersionSection>,
        libraryTree: LibraryCatalogTree,
        pluginTree: PluginCatalogTree
    ): List<RepositoryVersionSection> {
        val referencedVersionKeys =
            buildSet {
                libraryTree.roots.forEach { node ->
                    addLibraryVersionKeys(
                        node = node
                    )
                }
                pluginTree.roots.forEach { node ->
                    addPluginVersionKeys(
                        node = node
                    )
                }
            }

        return sections.map { section ->
            section.copy(
                versions =
                    section.versions.filterKeys(
                        predicate = referencedVersionKeys::contains
                    )
            )
        }
    }

    private fun MutableSet<String>.addLibraryVersionKeys(
        node: LibraryCatalogNode
    ) {
        node.entries.forEach { entry ->
            when (entry) {
                is LibraryCatalogEntry.Artifact -> {
                    addVisibleVersion(
                        version = entry.version
                    )
                }

                is LibraryCatalogEntry.ArtifactsBundle -> {
                    addVisibleVersion(
                        version = entry.version
                    )
                }
            }
        }
        node.children.forEach { child ->
            addLibraryVersionKeys(
                node = child
            )
        }
    }

    private fun MutableSet<String>.addPluginVersionKeys(
        node: PluginCatalogNode
    ) {
        node.version?.let { version ->
            addVisibleVersion(
                version = version
            )
        }
        node.children.forEach { child ->
            addPluginVersionKeys(
                node = child
            )
        }
    }

    private fun MutableSet<String>.addVisibleVersion(
        version: CatalogVersion
    ) {
        if (version.visible) {
            version.value?.let(::add)
        }
    }
}
