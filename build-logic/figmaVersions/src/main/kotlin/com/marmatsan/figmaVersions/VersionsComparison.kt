package com.marmatsan.figmaVersions

import me.tatarka.inject.annotations.Inject

data class VersionDifference(
    val repositoryValue: String,
    val figmaValue: String
)

data class VersionsComparisonResult(
    val missingInFigma: Map<String, String>,
    val extraInFigma: Map<String, String>,
    val changedValues: Map<String, VersionDifference>
) {
    val matches: Boolean
        get() = missingInFigma.isEmpty() && extraInFigma.isEmpty() && changedValues.isEmpty()

    fun report(): String = buildString {
        appendLine("Figma versions do not match build-logic/versions.properties.")

        if (missingInFigma.isNotEmpty()) {
            appendLine()
            appendLine("Missing in Figma:")
            missingInFigma.forEach { (key, value) ->
                appendLine("- $key=$value")
            }
        }

        if (extraInFigma.isNotEmpty()) {
            appendLine()
            appendLine("Extra in Figma:")
            extraInFigma.forEach { (key, value) ->
                appendLine("- $key=$value")
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
