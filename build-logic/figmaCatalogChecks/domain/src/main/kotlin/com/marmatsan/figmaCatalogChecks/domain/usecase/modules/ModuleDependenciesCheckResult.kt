package com.marmatsan.figmaCatalogChecks.domain.usecase.modules

import com.marmatsan.figmaCatalogChecks.domain.comparison.modules.ModuleDependenciesComparisonResult

sealed interface ModuleDependenciesCheckResult {
    data class Match(
        val sectionNodeId: String,
        val repositoryDependencyCount: Int
    ) : ModuleDependenciesCheckResult

    data class DifferentFiles(
        val fileKeys: List<String>
    ) : ModuleDependenciesCheckResult

    data class Mismatch(
        val comparison: ModuleDependenciesComparisonResult
    ) : ModuleDependenciesCheckResult
}
