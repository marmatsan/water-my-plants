package com.marmatsan.figmaCatalogChecks.plugin

import com.marmatsan.figmaCatalogChecks.data.FigmaCatalogTreesDataSource
import com.marmatsan.figmaCatalogChecks.data.FigmaFileContentClient
import com.marmatsan.figmaCatalogChecks.data.FigmaModulesDataSource
import com.marmatsan.figmaCatalogChecks.data.FigmaVersionsDataSource
import com.marmatsan.figmaCatalogChecks.data.ProjectCatalogTreesDataSource
import com.marmatsan.figmaCatalogChecks.data.ProjectModulesDataSource
import com.marmatsan.figmaCatalogChecks.data.RepositoryVersionsDataSource
import com.marmatsan.figmaCatalogChecks.domain.FigmaCatalogTreesPort
import com.marmatsan.figmaCatalogChecks.domain.FigmaModulesPort
import com.marmatsan.figmaCatalogChecks.domain.FigmaVersionsPort
import com.marmatsan.figmaCatalogChecks.domain.ProjectCatalogTreesPort
import com.marmatsan.figmaCatalogChecks.domain.ProjectModulesPort
import com.marmatsan.figmaCatalogChecks.domain.RepositoryVersionsPort
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@Component
internal abstract class FigmaCatalogChecksComponent {
    abstract val checker: FigmaVersionsChecker
    abstract val catalogTreeChecker: FigmaCatalogTreeChecker
    abstract val modulesChecker: FigmaModulesChecker

    @Provides
    protected fun figmaFileContentClient(): FigmaFileContentClient {
        return FigmaFileContentClient()
    }

    @Provides
    protected fun repositoryVersionsPort(dataSource: RepositoryVersionsDataSource): RepositoryVersionsPort =
        dataSource

    @Provides
    protected fun figmaVersionsPort(dataSource: FigmaVersionsDataSource): FigmaVersionsPort =
        dataSource

    @Provides
    protected fun projectCatalogTreesPort(dataSource: ProjectCatalogTreesDataSource): ProjectCatalogTreesPort =
        dataSource

    @Provides
    protected fun figmaCatalogTreesPort(dataSource: FigmaCatalogTreesDataSource): FigmaCatalogTreesPort =
        dataSource

    @Provides
    protected fun projectModulesPort(dataSource: ProjectModulesDataSource): ProjectModulesPort =
        dataSource

    @Provides
    protected fun figmaModulesPort(dataSource: FigmaModulesDataSource): FigmaModulesPort =
        dataSource
}
