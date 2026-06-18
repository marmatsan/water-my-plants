package com.marmatsan.figmaCatalogChecks.domain

import me.tatarka.inject.annotations.Inject

data class CatalogTreeDifference(
    val repositoryValue: String,
    val figmaValue: String
)

data class CatalogTreeComparisonResult(
    val subject: String,
    val missingInFigma: Map<String, String>,
    val extraInFigma: Map<String, String>,
    val changedValues: Map<String, CatalogTreeDifference>
) {
    val matches: Boolean
        get() = missingInFigma.isEmpty() && extraInFigma.isEmpty() && changedValues.isEmpty()

    fun report(): String = buildString {
        appendLine("Figma $subject tree does not match the repository catalog tree.")

        if (missingInFigma.isNotEmpty()) {
            appendLine()
            appendLine("Missing in Figma:")
            missingInFigma.forEach { (key, value) ->
                appendLine("- $key: $value")
            }
        }

        if (extraInFigma.isNotEmpty()) {
            appendLine()
            appendLine("Extra in Figma:")
            extraInFigma.forEach { (key, value) ->
                appendLine("- $key: $value")
            }
        }

        if (changedValues.isNotEmpty()) {
            appendLine()
            appendLine("Changed values:")
            changedValues.forEach { (key, difference) ->
                appendLine("- $key: repository=${difference.repositoryValue}, figma=${difference.figmaValue}")
            }
        }
    }.trimEnd()
}

@Inject
class LibraryCatalogTreeComparison {
    fun compare(
        repositoryTree: LibraryCatalogTree,
        figmaTree: LibraryCatalogTree
    ): CatalogTreeComparisonResult =
        compareCatalogFacts(
            subject = "library",
            repositoryFacts = repositoryTree.toFacts(),
            figmaFacts = figmaTree.toFacts()
        )

    private fun LibraryCatalogTree.toFacts(): Map<String, String> =
        roots
            .flatMap { root -> root.toFacts(parentPath = null) }
            .toMap()
            .toSortedMap()

    private fun LibraryCatalogNode.toFacts(parentPath: String?): List<Pair<String, String>> {
        val path = buildPath(parentPath, group)
        val nodeFact = "library $path" to "artifactsVisible=$artifactsVisible"
        val entryFacts = entries.map { entry -> entry.toFact(path) }
        val childFacts = children.flatMap { child -> child.toFacts(parentPath = path) }

        return listOf(nodeFact) + entryFacts + childFacts
    }

    private fun LibraryCatalogEntry.toFact(path: String): Pair<String, String> =
        when (this) {
            is LibraryCatalogEntry.Artifact -> {
                val key = "library $path artifact $artifact"
                val value = "version=${version.render()}"

                key to value
            }

            is LibraryCatalogEntry.ArtifactsBundle -> {
                val key = "library $path bundle $alias"
                val value = listOf(
                    "artifacts=${artifacts.renderList()}",
                    "version=${version.render()}"
                ).joinToString("; ")

                key to value
            }
        }
}

@Inject
class PluginCatalogTreeComparison {
    fun compare(
        repositoryTree: PluginCatalogTree,
        figmaTree: PluginCatalogTree
    ): CatalogTreeComparisonResult =
        compareCatalogFacts(
            subject = "plugin",
            repositoryFacts = repositoryTree.toFacts(),
            figmaFacts = figmaTree.toFacts()
        )

    private fun PluginCatalogTree.toFacts(): Map<String, String> =
        roots
            .flatMap { root -> root.toFacts(parentPath = null) }
            .toMap()
            .toSortedMap()

    private fun PluginCatalogNode.toFacts(parentPath: String?): List<Pair<String, String>> {
        val path = buildPath(parentPath, id)
        val nodeFact = "plugin $path" to "version=${version.render()}"
        val childFacts = children.flatMap { child -> child.toFacts(parentPath = path) }

        return listOf(nodeFact) + childFacts
    }
}

private fun compareCatalogFacts(
    subject: String,
    repositoryFacts: Map<String, String>,
    figmaFacts: Map<String, String>
): CatalogTreeComparisonResult {
    val missingInFigma = repositoryFacts
        .filterKeys { key -> key !in figmaFacts }
        .toSortedMap()

    val extraInFigma = figmaFacts
        .filterKeys { key -> key !in repositoryFacts }
        .toSortedMap()

    val changedValues = repositoryFacts
        .keys
        .intersect(figmaFacts.keys)
        .mapNotNull { key ->
            val repositoryValue = repositoryFacts.getValue(key)
            val figmaValue = figmaFacts.getValue(key)

            if (repositoryValue == figmaValue) {
                null
            } else {
                key to CatalogTreeDifference(
                    repositoryValue = repositoryValue,
                    figmaValue = figmaValue
                )
            }
        }
        .toMap()
        .toSortedMap()

    return CatalogTreeComparisonResult(
        subject = subject,
        missingInFigma = missingInFigma,
        extraInFigma = extraInFigma,
        changedValues = changedValues
    )
}

private fun buildPath(
    parentPath: String?,
    segment: String
): String =
    if (parentPath == null) {
        segment
    } else {
        "$parentPath.$segment"
    }

private fun CatalogVersion?.render(): String =
    when {
        this == null -> "hidden"
        visible -> value.orEmpty()
        else -> "hidden"
    }

private fun List<String>.renderList(): String =
    sorted().joinToString(
        prefix = "[",
        postfix = "]"
    )
