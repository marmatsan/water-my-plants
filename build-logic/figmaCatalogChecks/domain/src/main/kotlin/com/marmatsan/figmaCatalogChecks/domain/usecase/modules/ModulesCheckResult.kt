package com.marmatsan.figmaCatalogChecks.domain.usecase.modules

import com.marmatsan.figmaCatalogChecks.domain.comparison.modules.ModuleNamesComparisonResult

sealed interface ModulesCheckResult {
    data class Match(
        val moduleComponentNodeId: String,
        val repositoryModuleCount: Int
    ) : ModulesCheckResult

    data class DifferentFiles(
        val fileKeys: List<String>
    ) : ModulesCheckResult

    data class Mismatch(
        val comparison: ModuleNamesComparisonResult
    ) : ModulesCheckResult
}
