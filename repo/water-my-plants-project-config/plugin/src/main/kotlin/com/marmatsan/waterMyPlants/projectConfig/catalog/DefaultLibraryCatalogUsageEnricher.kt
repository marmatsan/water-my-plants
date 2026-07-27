package com.marmatsan.waterMyPlants.projectConfig.catalog

import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogEntry
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogNode
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogTree

/** Pure enrichment of library catalog entries with their repository usage. */
internal class DefaultLibraryCatalogUsageEnricher : LibraryCatalogUsageEnricher {
    /** Applies [mainUsages] and [conventionPluginUsages] to every entry in [tree]. */
    override fun enrich(
        tree: LibraryCatalogTree,
        mainUsages: MainLibraryUsages,
        conventionPluginUsages: ConventionPluginLibraryUsages,
    ): LibraryCatalogTree =
        tree.copy(
            roots =
                tree.roots.map { node ->
                    enrichNode(
                        node = node,
                        mainUsages = mainUsages,
                        conventionPluginUsages = conventionPluginUsages,
                    )
                },
        )

    private fun enrichNode(
        node: LibraryCatalogNode,
        mainUsages: MainLibraryUsages,
        conventionPluginUsages: ConventionPluginLibraryUsages,
        parentGroup: String = "",
    ): LibraryCatalogNode {
        val groupPath =
            listOf(
                parentGroup,
                node.group,
            ).filter(String::isNotBlank)
                .joinToString(".")

        return node.copy(
            entries =
                node.entries.map { entry ->
                    enrichEntry(
                        entry = entry,
                        group = groupPath,
                        mainUsages = mainUsages,
                        conventionPluginUsages = conventionPluginUsages,
                    )
                },
            children =
                node.children.map { child ->
                    enrichNode(
                        node = child,
                        mainUsages = mainUsages,
                        conventionPluginUsages = conventionPluginUsages,
                        parentGroup = groupPath,
                    )
                },
        )
    }

    private fun enrichEntry(
        entry: LibraryCatalogEntry,
        group: String,
        mainUsages: MainLibraryUsages,
        conventionPluginUsages: ConventionPluginLibraryUsages,
    ): LibraryCatalogEntry =
        when (entry) {
            is LibraryCatalogEntry.Artifact -> {
                val coordinate = "$group:${entry.artifact}"
                entry.copy(
                    requiredByModules =
                        (
                            mainUsages.coordinates[coordinate].orEmpty() +
                                mainUsages.aliases[
                                    libraryAlias(
                                        libraryGroup = group,
                                        artifact = entry.artifact,
                                    ),
                                ].orEmpty()
                        ).sorted(),
                    providedByConventionPlugins = conventionPluginUsages.coordinates[coordinate].orEmpty(),
                    configuredByConventionPlugins =
                        conventionPluginUsages.configuredCoordinates[coordinate].orEmpty(),
                )
            }

            is LibraryCatalogEntry.ArtifactsBundle -> {
                entry.copy(
                    requiredByModules = mainUsages.bundles[entry.alias].orEmpty().sorted(),
                    providedByConventionPlugins = conventionPluginUsages.bundles[entry.alias].orEmpty(),
                )
            }
        }

    private fun libraryAlias(
        libraryGroup: String,
        artifact: String,
    ): String {
        val groupSegments = libraryGroup.split(".")
        var groupSuffix = ""
        var artifactAliasSegment: String? = null

        for (index in groupSegments.lastIndex downTo 0) {
            groupSuffix =
                if (groupSuffix.isEmpty()) {
                    groupSegments[index]
                } else {
                    "${groupSegments[index]}-$groupSuffix"
                }

            artifactAliasSegment =
                when {
                    artifact == groupSuffix -> ""

                    artifact.startsWith(
                        prefix = "$groupSuffix-",
                    ) -> artifact.removePrefix("$groupSuffix-")

                    else -> null
                }

            if (artifactAliasSegment != null) {
                break
            }
        }

        val normalizedArtifactAliasSegment =
            (artifactAliasSegment ?: artifact).replace(
                "-",
                ".",
            )

        return if (artifactAliasSegment?.isEmpty() == true) {
            libraryGroup
        } else {
            "$libraryGroup.$normalizedArtifactAliasSegment"
        }
    }
}
