package com.marmatsan.figmaCatalogChecks.domain.usecase.versions

import com.marmatsan.figmaCatalogChecks.domain.comparison.versions.VersionsComparisonResult

sealed interface FigmaVersionsCheckResult {
    data class Match(
        val sectionNodeId: String,
        val repositoryVersionCount: Int
    ) : FigmaVersionsCheckResult

    data class DifferentFiles(
        val fileKeys: List<String>
    ) : FigmaVersionsCheckResult

    data class VersionsMismatch(
        val comparison: VersionsComparisonResult
    ) : FigmaVersionsCheckResult
}
