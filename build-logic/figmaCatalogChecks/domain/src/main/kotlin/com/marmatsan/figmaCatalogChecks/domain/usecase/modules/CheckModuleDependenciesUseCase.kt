package com.marmatsan.figmaCatalogChecks.domain.usecase.modules

import com.marmatsan.figmaCatalogChecks.domain.comparison.modules.ModuleDependenciesComparison
import com.marmatsan.figmaCatalogChecks.domain.port.modules.FigmaModuleDependenciesPort
import com.marmatsan.figmaCatalogChecks.domain.port.modules.FigmaModuleDependenciesSource
import com.marmatsan.figmaCatalogChecks.domain.port.modules.ProjectModuleDependenciesPort
import com.marmatsan.figmaCatalogChecks.domain.port.modules.ProjectModuleDependenciesSource
import me.tatarka.inject.annotations.Inject

@Inject
class CheckModuleDependenciesUseCase(
    private val projectModuleDependenciesPort: ProjectModuleDependenciesPort,
    private val figmaModuleDependenciesPort: FigmaModuleDependenciesPort,
    private val comparison: ModuleDependenciesComparison
) {
    fun execute(request: CheckModuleDependenciesUseCaseRequest): ModuleDependenciesCheckResult {
        val fileKeys = listOf(
            request.page.fileKey,
            request.section.fileKey
        ).distinct()

        if (fileKeys.size != 1) {
            return ModuleDependenciesCheckResult.DifferentFiles(fileKeys.sorted())
        }

        val repositoryDependencies = projectModuleDependenciesPort.readModuleDependencies(
            ProjectModuleDependenciesSource(
                rootDirPath = request.rootDirPath,
                scope = request.scope
            )
        )
        val figmaDependencies = figmaModuleDependenciesPort.readModuleDependencies(
            FigmaModuleDependenciesSource(
                section = request.section,
                token = request.token
            )
        )
        val comparisonResult = comparison.compare(
            repositoryDependencies = repositoryDependencies,
            figmaDependencies = figmaDependencies
        )

        if (!comparisonResult.matches) {
            return ModuleDependenciesCheckResult.Mismatch(comparisonResult)
        }

        return ModuleDependenciesCheckResult.Match(
            sectionNodeId = request.section.nodeId,
            repositoryDependencyCount = repositoryDependencies.size
        )
    }
}
