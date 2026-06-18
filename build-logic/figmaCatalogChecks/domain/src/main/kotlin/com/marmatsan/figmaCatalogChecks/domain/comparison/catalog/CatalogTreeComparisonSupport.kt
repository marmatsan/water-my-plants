package com.marmatsan.figmaCatalogChecks.domain.comparison.catalog

import com.marmatsan.figmaCatalogChecks.domain.model.catalog.CatalogVersion

internal fun compareCatalogFacts(
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

internal fun buildPath(
    parentPath: String?,
    segment: String
): String =
    if (parentPath == null) {
        segment
    } else {
        "$parentPath.$segment"
    }

internal fun CatalogVersion?.render(): String =
    when {
        this == null -> "hidden"
        visible -> value.orEmpty()
        else -> "hidden"
    }

internal fun List<String>.renderList(): String =
    sorted().joinToString(
        prefix = "[",
        postfix = "]"
    )
