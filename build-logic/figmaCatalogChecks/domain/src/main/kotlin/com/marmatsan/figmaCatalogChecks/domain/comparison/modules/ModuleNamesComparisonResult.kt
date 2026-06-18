package com.marmatsan.figmaCatalogChecks.domain.comparison.modules

data class ModuleNamesComparisonResult(
    val missingInFigma: Set<String>,
    val extraInFigma: Set<String>
) {
    val matches: Boolean
        get() = missingInFigma.isEmpty() && extraInFigma.isEmpty()

    fun report(): String = buildString {
        appendLine("Figma .module variants do not match the repository modules.")

        if (missingInFigma.isNotEmpty()) {
            appendLine()
            appendLine("Missing in Figma:")
            missingInFigma.forEach { module ->
                appendLine("- $module")
            }
        }

        if (extraInFigma.isNotEmpty()) {
            appendLine()
            appendLine("Extra in Figma:")
            extraInFigma.forEach { module ->
                appendLine("- $module")
            }
        }
    }.trimEnd()
}
