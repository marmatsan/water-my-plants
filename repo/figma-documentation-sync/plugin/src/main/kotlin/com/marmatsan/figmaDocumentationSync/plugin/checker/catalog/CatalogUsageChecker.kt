package com.marmatsan.figmaDocumentationSync.plugin.checker.catalog

import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogEntry
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogNode
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogTree
import com.marmatsan.figmaDocumentationSync.domain.port.catalog.ProjectCatalogTreeSource
import com.marmatsan.figmaDocumentationSync.domain.port.catalog.ProjectCatalogTreesPort
import com.marmatsan.figmaDocumentationSync.domain.port.gradle.IncludedBuildSource
import com.marmatsan.figmaDocumentationSync.plugin.generator.FigmaDesignModelIncludedBuildSource
import me.tatarka.inject.annotations.Inject

/**
 * Verifies that dependency catalogs only declare entries that are used by the
 * repository.
 *
 * This checker intentionally scopes itself to dependency catalogs. Repository
 * owned custom Gradle plugin inventories are documentation, not dependency
 * catalogs, so they may contain plugins that no module applies yet.
 */
@Inject
internal class CatalogUsageChecker(
    private val projectCatalogTreesPort: ProjectCatalogTreesPort,
) {
    /** Compares catalog declarations with all supported repository usage sources in [request]. */
    fun check(
        request: CatalogUsageCheckRequest,
    ): CatalogUsageCheckResult {
        val includedBuilds =
            request.includedBuilds.map(
                transform = FigmaDesignModelIncludedBuildSource::toDomainSource,
            )
        val conventionPluginIncludedBuilds = includedBuilds.filter(IncludedBuildSource::publishesConventionPlugins)
        val unusedEntries = mutableListOf<UnusedCatalogEntry>()

        val dependencyDslSource =
            ProjectCatalogTreeSource.DependenciesDslVersionAliases(
                rootDirPath = request.projectRootDirectory.absolutePath,
                providerClassName = request.dependencyCatalogProviderClassName,
                conventionPluginIncludedBuilds = conventionPluginIncludedBuilds,
            )
        unusedEntries +=
            projectCatalogTreesPort
                .readLibraryTree(dependencyDslSource)
                .unusedEntries(
                    catalogName = "${request.primaryCatalogModelName}.libraries",
                )
        unusedEntries +=
            projectCatalogTreesPort
                .readPluginTree(dependencyDslSource)
                .unusedEntries(
                    catalogName = "${request.primaryCatalogModelName}.plugins",
                )

        request.includedBuilds
            .filter(FigmaDesignModelIncludedBuildSource::publishesCatalogs)
            .forEach { includedBuild ->
                val source =
                    ProjectCatalogTreeSource.IncludedBuildSettings(
                        includedBuild = includedBuild.toDomainSource(),
                    )
                unusedEntries +=
                    projectCatalogTreesPort
                        .readLibraryTree(source)
                        .unusedEntries(
                            catalogName = "${includedBuild.modelName}.libraries",
                        )
                unusedEntries +=
                    projectCatalogTreesPort
                        .readPluginTree(source)
                        .unusedEntries(
                            catalogName = "${includedBuild.modelName}.plugins",
                        )
            }

        return CatalogUsageCheckResult(
            unusedEntries =
                unusedEntries.sortedWith(
                    compareBy(
                        UnusedCatalogEntry::catalogName,
                        UnusedCatalogEntry::entry,
                    ),
                ),
        )
    }
}

private fun LibraryCatalogTree.unusedEntries(
    catalogName: String,
): List<UnusedCatalogEntry> =
    roots.flatMap { root ->
        root.unusedEntries(
            catalogName = catalogName,
        )
    }

private fun LibraryCatalogNode.unusedEntries(
    catalogName: String,
    parentGroup: String = "",
): List<UnusedCatalogEntry> {
    val groupPath =
        listOf(
            parentGroup,
            group,
        ).filter(String::isNotBlank)
            .joinToString(".")

    return entries
        .filterNot(LibraryCatalogEntry::hasUsage)
        .map { entry ->
            UnusedCatalogEntry(
                catalogName = catalogName,
                entry =
                    entry.catalogPath(
                        groupPath = groupPath,
                    ),
            )
        } +
        children.flatMap { child ->
            child.unusedEntries(
                catalogName = catalogName,
                parentGroup = groupPath,
            )
        }
}

private fun LibraryCatalogEntry.hasUsage(): Boolean =
    when (this) {
        is LibraryCatalogEntry.Artifact -> {
            requiredByModules.isNotEmpty() ||
                providedByConventionPlugins.isNotEmpty() ||
                configuredByConventionPlugins.isNotEmpty()
        }

        is LibraryCatalogEntry.ArtifactsBundle -> {
            requiredByModules.isNotEmpty() ||
                providedByConventionPlugins.isNotEmpty()
        }
    }

private fun LibraryCatalogEntry.catalogPath(
    groupPath: String,
): String =
    when (this) {
        is LibraryCatalogEntry.Artifact -> "$groupPath:$artifact"
        is LibraryCatalogEntry.ArtifactsBundle -> "$groupPath bundle '$alias'"
    }

private fun PluginCatalogTree.unusedEntries(
    catalogName: String,
): List<UnusedCatalogEntry> =
    roots.flatMap { root ->
        root.unusedEntries(
            catalogName = catalogName,
        )
    }

private fun PluginCatalogNode.unusedEntries(
    catalogName: String,
    parentId: String = "",
): List<UnusedCatalogEntry> {
    val pluginId =
        listOf(
            parentId,
            id,
        ).filter(String::isNotBlank)
            .joinToString(".")
    val currentEntry =
        if (isCatalogEntry && !hasUsage) {
            listOf(
                UnusedCatalogEntry(
                    catalogName = catalogName,
                    entry = pluginId,
                ),
            )
        } else {
            emptyList()
        }

    return currentEntry +
        children.flatMap { child ->
            child.unusedEntries(
                catalogName = catalogName,
                parentId = pluginId,
            )
        }
}

private val PluginCatalogNode.isCatalogEntry: Boolean
    get() = version != null

private val PluginCatalogNode.hasUsage: Boolean
    get() = appliedToModules.isNotEmpty() || providedByConventionPlugins.isNotEmpty()
