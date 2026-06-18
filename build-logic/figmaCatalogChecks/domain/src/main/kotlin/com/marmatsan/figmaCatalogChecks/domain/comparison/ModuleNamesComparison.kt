package com.marmatsan.figmaCatalogChecks.domain.comparison

import com.marmatsan.figmaCatalogChecks.domain.model.*

import me.tatarka.inject.annotations.Inject

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

@Inject
class ModuleNamesComparison {
    fun compare(
        repositoryModules: Set<String>,
        figmaModules: Set<String>
    ): ModuleNamesComparisonResult =
        ModuleNamesComparisonResult(
            missingInFigma = repositoryModules
                .filter { module -> module !in figmaModules }
                .toSortedSet(),
            extraInFigma = figmaModules
                .filter { module -> module !in repositoryModules }
                .toSortedSet()
        )
}
