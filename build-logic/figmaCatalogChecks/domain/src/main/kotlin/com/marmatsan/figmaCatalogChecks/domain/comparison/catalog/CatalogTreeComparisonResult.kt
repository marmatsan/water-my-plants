package com.marmatsan.figmaCatalogChecks.domain.comparison.catalog

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
