package com.marmatsan.figmaCatalogChecks.domain.usecase

import com.marmatsan.figmaCatalogChecks.domain.model.*
import com.marmatsan.figmaCatalogChecks.domain.comparison.*
import com.marmatsan.figmaCatalogChecks.domain.port.*

import me.tatarka.inject.annotations.Inject

data class FigmaVersionsCheckInput(
    val page: FigmaNodeReference,
    val section: FigmaNodeReference,
    val versionComponent: FigmaNodeReference,
    val repositoryVersions: Map<String, String>,
    val figmaVersions: Map<String, String>
)

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

@Inject
class FigmaVersionsCheck(
    private val versionsComparison: VersionsComparison
) {
    fun check(
        input: FigmaVersionsCheckInput
    ): FigmaVersionsCheckResult {
        val fileKeys = listOf(
            input.page.fileKey,
            input.section.fileKey,
            input.versionComponent.fileKey
        ).distinct()

        if (fileKeys.size != 1) {
            return FigmaVersionsCheckResult.DifferentFiles(fileKeys.sorted())
        }

        val comparison = versionsComparison.compare(
            repositoryVersions = input.repositoryVersions,
            figmaVersions = input.figmaVersions
        )

        if (!comparison.matches) {
            return FigmaVersionsCheckResult.VersionsMismatch(comparison)
        }

        return FigmaVersionsCheckResult.Match(
            sectionNodeId = input.section.nodeId,
            repositoryVersionCount = input.repositoryVersions.size
        )
    }
}
