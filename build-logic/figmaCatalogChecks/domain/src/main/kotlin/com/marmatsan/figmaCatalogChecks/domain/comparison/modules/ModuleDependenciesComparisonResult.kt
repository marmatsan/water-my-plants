package com.marmatsan.figmaCatalogChecks.domain.comparison.modules

import com.marmatsan.figmaCatalogChecks.domain.model.modules.ModuleDependency

data class ModuleDependenciesComparisonResult(
    val missingInFigma: Set<ModuleDependency>,
    val extraInFigma: Set<ModuleDependency>
) {
    val matches: Boolean
        get() = missingInFigma.isEmpty() && extraInFigma.isEmpty()

    fun report(): String = buildString {
        appendLine("Figma module dependencies do not match the Gradle project.")

        if (missingInFigma.isNotEmpty()) {
            appendLine()
            appendLine("Missing in Figma:")
            missingInFigma.forEach { dependency ->
                appendLine("- ${dependency.render()}")
            }
        }

        if (extraInFigma.isNotEmpty()) {
            appendLine()
            appendLine("Extra in Figma:")
            extraInFigma.forEach { dependency ->
                appendLine("- ${dependency.render()}")
            }
        }
    }.trimEnd()
}
