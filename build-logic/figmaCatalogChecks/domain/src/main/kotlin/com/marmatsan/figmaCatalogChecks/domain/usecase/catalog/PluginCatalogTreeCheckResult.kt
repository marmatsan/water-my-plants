package com.marmatsan.figmaCatalogChecks.domain.usecase.catalog

import com.marmatsan.figmaCatalogChecks.domain.comparison.catalog.CatalogTreeComparisonResult
import com.marmatsan.figmaCatalogChecks.domain.comparison.usage.CatalogUsageComparisonResult

sealed interface PluginCatalogTreeCheckResult {
    data class Match(
        val sectionNodeId: String,
        val repositoryNodeCount: Int
    ) : PluginCatalogTreeCheckResult

    data class DifferentFiles(
        val fileKeys: List<String>
    ) : PluginCatalogTreeCheckResult

    data class Mismatch(
        val comparison: CatalogTreeComparisonResult
    ) : PluginCatalogTreeCheckResult

    data class UsageMismatch(
        val comparison: CatalogUsageComparisonResult
    ) : PluginCatalogTreeCheckResult
}
