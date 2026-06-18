package com.marmatsan.figmaCatalogChecks.domain.usecase.versions

import com.marmatsan.figmaCatalogChecks.domain.port.versions.FigmaVersionsPort
import com.marmatsan.figmaCatalogChecks.domain.port.versions.FigmaVersionsSource
import com.marmatsan.figmaCatalogChecks.domain.port.versions.RepositoryVersionsPort
import com.marmatsan.figmaCatalogChecks.domain.port.versions.VersionsFileSource
import me.tatarka.inject.annotations.Inject

@Inject
class CheckVersionsUseCase(
    private val repositoryVersionsPort: RepositoryVersionsPort,
    private val figmaVersionsPort: FigmaVersionsPort,
    private val figmaVersionsCheck: FigmaVersionsCheck
) {
    fun execute(request: CheckVersionsUseCaseRequest): FigmaVersionsCheckResult =
        figmaVersionsCheck.check(
            FigmaVersionsCheckInput(
                page = request.page,
                section = request.section,
                versionComponent = request.versionComponent,
                repositoryVersions = repositoryVersionsPort.readVersions(
                    VersionsFileSource(path = request.versionsFilePath)
                ),
                figmaVersions = figmaVersionsPort.readVersions(
                    FigmaVersionsSource(
                        section = request.section,
                        versionComponent = request.versionComponent,
                        token = request.token
                    )
                )
            )
        )
}
