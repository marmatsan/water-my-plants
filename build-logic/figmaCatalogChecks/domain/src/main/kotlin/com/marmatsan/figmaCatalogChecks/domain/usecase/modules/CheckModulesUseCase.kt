package com.marmatsan.figmaCatalogChecks.domain.usecase.modules

import com.marmatsan.figmaCatalogChecks.domain.comparison.modules.ModuleNamesComparison
import com.marmatsan.figmaCatalogChecks.domain.port.modules.FigmaModuleComponentSource
import com.marmatsan.figmaCatalogChecks.domain.port.modules.FigmaModulesPort
import com.marmatsan.figmaCatalogChecks.domain.port.modules.ProjectModulesPort
import com.marmatsan.figmaCatalogChecks.domain.port.modules.ProjectModulesSource
import me.tatarka.inject.annotations.Inject

@Inject
class CheckModulesUseCase(
    private val projectModulesPort: ProjectModulesPort,
    private val figmaModulesPort: FigmaModulesPort,
    private val moduleNamesComparison: ModuleNamesComparison
) {
    fun execute(request: CheckModulesUseCaseRequest): ModulesCheckResult {
        val fileKeys = listOf(
            request.page.fileKey,
            request.moduleComponent.fileKey
        ).distinct()

        if (fileKeys.size != 1) {
            return ModulesCheckResult.DifferentFiles(fileKeys.sorted())
        }

        val repositoryModules = projectModulesPort.readModules(
            ProjectModulesSource(
                rootSettingsFilePath = request.rootSettingsFilePath,
                buildLogicSettingsFilePath = request.buildLogicSettingsFilePath
            )
        )
        val figmaModules = figmaModulesPort.readModules(
            FigmaModuleComponentSource(
                component = request.moduleComponent,
                token = request.token
            )
        )
        val comparison = moduleNamesComparison.compare(
            repositoryModules = repositoryModules,
            figmaModules = figmaModules
        )

        if (!comparison.matches) {
            return ModulesCheckResult.Mismatch(comparison)
        }

        return ModulesCheckResult.Match(
            moduleComponentNodeId = request.moduleComponent.nodeId,
            repositoryModuleCount = repositoryModules.size
        )
    }
}
