package com.marmatsan.figmaCatalogChecks.domain.comparison.modules


import me.tatarka.inject.annotations.Inject

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
