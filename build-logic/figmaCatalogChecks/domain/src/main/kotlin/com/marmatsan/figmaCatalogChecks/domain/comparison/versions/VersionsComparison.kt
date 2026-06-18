package com.marmatsan.figmaCatalogChecks.domain.comparison.versions


import me.tatarka.inject.annotations.Inject

@Inject
class VersionsComparison {
    fun compare(
        repositoryVersions: Map<String, String>,
        figmaVersions: Map<String, String>
    ): VersionsComparisonResult {
        val missingInFigma = repositoryVersions
            .filterKeys { key -> key !in figmaVersions }
            .toSortedMap()

        val extraInFigma = figmaVersions
            .filterKeys { key -> key !in repositoryVersions }
            .toSortedMap()

        val changedValues = repositoryVersions
            .keys
            .intersect(figmaVersions.keys)
            .mapNotNull { key ->
                val repositoryValue = repositoryVersions.getValue(key)
                val figmaValue = figmaVersions.getValue(key)

                if (repositoryValue == figmaValue) {
                    null
                } else {
                    key to VersionDifference(
                        repositoryValue = repositoryValue,
                        figmaValue = figmaValue
                    )
                }
            }
            .toMap()
            .toSortedMap()

        return VersionsComparisonResult(
            missingInFigma = missingInFigma,
            extraInFigma = extraInFigma,
            changedValues = changedValues
        )
    }
}
