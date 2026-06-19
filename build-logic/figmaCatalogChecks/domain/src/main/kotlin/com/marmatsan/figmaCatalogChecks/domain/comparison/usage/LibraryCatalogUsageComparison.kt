package com.marmatsan.figmaCatalogChecks.domain.comparison.usage

import com.marmatsan.figmaCatalogChecks.domain.comparison.catalog.buildPath
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.LibraryCatalogEntry
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.LibraryCatalogNode
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaCatalogChecks.domain.model.usage.LibraryCatalogUsageKey
import com.marmatsan.figmaCatalogChecks.domain.model.usage.ProjectCatalogUsage
import me.tatarka.inject.annotations.Inject

@Inject
class LibraryCatalogUsageComparison {
    fun compare(
        figmaTree: LibraryCatalogTree,
        projectUsage: ProjectCatalogUsage
    ): CatalogUsageComparisonResult {
        val expectedModulesByKey = figmaTree.expectedModulesByKey()
        val actualModulesByKey = projectUsage.actualLibraryModulesByKey(expectedModulesByKey.keys)

        return compareUsages(
            subject = "library",
            expectedModulesByKey = expectedModulesByKey.mapKeys { (key, _) -> key.render() },
            actualModulesByKey = actualModulesByKey.mapKeys { (key, _) -> key.render() }
        )
    }

    private fun LibraryCatalogTree.expectedModulesByKey(): Map<LibraryCatalogUsageKey, List<String>> =
        roots
            .flatMap { root -> root.expectedModulesByKey(parentPath = null) }
            .toMap()

    private fun LibraryCatalogNode.expectedModulesByKey(
        parentPath: String?
    ): List<Pair<LibraryCatalogUsageKey, List<String>>> {
        val path = buildPath(parentPath, group)
        val entryUsages = entries.map { entry -> entry.toUsage(path) }
        val childUsages = children.flatMap { child -> child.expectedModulesByKey(path) }

        return entryUsages + childUsages
    }

    private fun LibraryCatalogEntry.toUsage(
        group: String
    ): Pair<LibraryCatalogUsageKey, List<String>> =
        when (this) {
            is LibraryCatalogEntry.Artifact -> LibraryCatalogUsageKey.Artifact(
                group = group,
                artifact = artifact
            ) to requiredByModules

            is LibraryCatalogEntry.ArtifactsBundle -> LibraryCatalogUsageKey.Bundle(
                alias = alias
            ) to requiredByModules
        }

    private fun ProjectCatalogUsage.actualLibraryModulesByKey(
        expectedKeys: Set<LibraryCatalogUsageKey>
    ): Map<LibraryCatalogUsageKey, List<String>> =
        expectedKeys.associateWith { expectedKey ->
            val expectedAccessor = expectedKey.accessor()

            libraryKeysByModule.keys
                .plus(libraryAccessorsByModule.keys)
                .filter { module ->
                    expectedKey in libraryKeysByModule[module].orEmpty() ||
                        expectedAccessor in libraryAccessorsByModule[module].orEmpty()
                }
                .distinct()
                .sorted()
        }

    private fun LibraryCatalogUsageKey.accessor(): String =
        when (this) {
            is LibraryCatalogUsageKey.Artifact -> libraryAlias(
                libraryGroup = group,
                artifact = artifact
            )

            is LibraryCatalogUsageKey.Bundle -> "bundles.$alias"
        }

    private fun libraryAlias(
        libraryGroup: String,
        artifact: String
    ): String {
        val groupSegments = libraryGroup.split(".")
        var groupSuffix = ""
        var artifactAliasSegment: String? = null

        for (index in groupSegments.lastIndex downTo 0) {
            groupSuffix = if (groupSuffix.isEmpty()) {
                groupSegments[index]
            } else {
                "${groupSegments[index]}-$groupSuffix"
            }

            artifactAliasSegment = when {
                artifact == groupSuffix -> ""
                artifact.startsWith("$groupSuffix-") -> artifact.removePrefix("$groupSuffix-")
                else -> null
            }

            if (artifactAliasSegment != null) {
                break
            }
        }

        val normalizedArtifactAliasSegment = (artifactAliasSegment ?: artifact).replace("-", ".")

        return if (artifactAliasSegment?.isEmpty() == true) {
            libraryGroup
        } else {
            "$libraryGroup.$normalizedArtifactAliasSegment"
        }
    }

    private fun LibraryCatalogUsageKey.render(): String =
        when (this) {
            is LibraryCatalogUsageKey.Artifact -> "library $group artifact $artifact"
            is LibraryCatalogUsageKey.Bundle -> "library bundle $alias"
        }
}
