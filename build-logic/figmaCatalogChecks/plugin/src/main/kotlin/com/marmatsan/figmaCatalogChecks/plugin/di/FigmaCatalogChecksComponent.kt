package com.marmatsan.figmaCatalogChecks.plugin.di

import com.marmatsan.figmaCatalogChecks.data.datasource.*
import com.marmatsan.figmaCatalogChecks.data.figma.*
import com.marmatsan.figmaCatalogChecks.domain.port.*
import com.marmatsan.figmaCatalogChecks.plugin.checker.*

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
