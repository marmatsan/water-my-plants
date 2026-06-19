package com.marmatsan.figmaCatalogChecks.domain.comparison.usage

data class CatalogUsageComparisonResult(
    val subject: String,
    val changedValues: Map<String, CatalogUsageDifference>
) {
    val matches: Boolean = changedValues.isEmpty()

    fun report(): String =
        buildString {
            appendLine("Figma $subject usages do not match the Gradle project.")
            appendLine()
            appendLine("Changed values:")
            changedValues.forEach { (key, difference) ->
                appendLine(
                    "- $key: repository=${difference.repositoryModules.renderModules()}, " +
                        "figma=${difference.figmaModules.renderModules()}"
                )
            }
        }.trimEnd()
}

private fun List<String>.renderModules(): String =
    sorted().joinToString(
        prefix = "[",
        postfix = "]"
    )
