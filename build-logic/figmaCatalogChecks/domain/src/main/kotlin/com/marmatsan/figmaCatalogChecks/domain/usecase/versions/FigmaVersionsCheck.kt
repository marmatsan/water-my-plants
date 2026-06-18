package com.marmatsan.figmaCatalogChecks.domain.usecase.versions


import com.marmatsan.figmaCatalogChecks.domain.comparison.versions.VersionsComparison
import me.tatarka.inject.annotations.Inject

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
