package com.marmatsan.figmaCatalogChecks.plugin.di

import com.marmatsan.figmaCatalogChecks.data.datasource.catalog.FigmaCatalogTreesDataSource
import com.marmatsan.figmaCatalogChecks.data.datasource.catalog.ProjectCatalogTreesDataSource
import com.marmatsan.figmaCatalogChecks.data.datasource.modules.FigmaModulesDataSource
import com.marmatsan.figmaCatalogChecks.data.datasource.modules.ProjectModulesDataSource
import com.marmatsan.figmaCatalogChecks.data.datasource.versions.FigmaVersionsDataSource
import com.marmatsan.figmaCatalogChecks.data.datasource.versions.RepositoryVersionsDataSource
import com.marmatsan.figmaCatalogChecks.data.figma.client.FigmaFileContentClient
import com.marmatsan.figmaCatalogChecks.domain.port.catalog.FigmaCatalogTreesPort
import com.marmatsan.figmaCatalogChecks.domain.port.catalog.ProjectCatalogTreesPort
import com.marmatsan.figmaCatalogChecks.domain.port.modules.FigmaModulesPort
import com.marmatsan.figmaCatalogChecks.domain.port.modules.ProjectModulesPort
import com.marmatsan.figmaCatalogChecks.domain.port.versions.FigmaVersionsPort
import com.marmatsan.figmaCatalogChecks.domain.port.versions.RepositoryVersionsPort
import com.marmatsan.figmaCatalogChecks.plugin.checker.catalog.FigmaCatalogTreeChecker
import com.marmatsan.figmaCatalogChecks.plugin.checker.modules.FigmaModulesChecker
import com.marmatsan.figmaCatalogChecks.plugin.checker.versions.FigmaVersionsChecker
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
