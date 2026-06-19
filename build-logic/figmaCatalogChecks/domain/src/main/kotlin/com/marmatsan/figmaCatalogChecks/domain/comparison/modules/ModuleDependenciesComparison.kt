package com.marmatsan.figmaCatalogChecks.domain.comparison.modules

import com.marmatsan.figmaCatalogChecks.domain.model.modules.ModuleDependency
import me.tatarka.inject.annotations.Inject

@Inject
class ModuleDependenciesComparison {
    fun compare(
        repositoryDependencies: Set<ModuleDependency>,
        figmaDependencies: Set<ModuleDependency>
    ): ModuleDependenciesComparisonResult =
        ModuleDependenciesComparisonResult(
            missingInFigma = repositoryDependencies
                .filter { dependency -> dependency !in figmaDependencies }
                .toSortedSet(),
            extraInFigma = figmaDependencies
                .filter { dependency -> dependency !in repositoryDependencies }
                .toSortedSet()
        )
}
