package com.marmatsan.figmaCatalogChecks.domain.usecase.catalog

import com.marmatsan.figmaCatalogChecks.domain.comparison.catalog.CatalogTreeComparisonResult
import com.marmatsan.figmaCatalogChecks.domain.comparison.usage.CatalogUsageComparisonResult

sealed interface LibraryCatalogTreeCheckResult {
    data class Match(
        val sectionNodeId: String,
        val repositoryNodeCount: Int
    ) : LibraryCatalogTreeCheckResult

    data class DifferentFiles(
        val fileKeys: List<String>
    ) : LibraryCatalogTreeCheckResult

    data class Mismatch(
        val comparison: CatalogTreeComparisonResult
    ) : LibraryCatalogTreeCheckResult

    data class UsageMismatch(
        val comparison: CatalogUsageComparisonResult
    ) : LibraryCatalogTreeCheckResult
}
