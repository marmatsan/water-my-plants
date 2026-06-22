package com.marmatsan.figmaDesignSync.plugin.di

import com.marmatsan.figmaDesignSync.data.datasource.catalog.ProjectCatalogTreesDataSource
import com.marmatsan.figmaDesignSync.data.datasource.modules.ProjectModuleDependenciesDataSource
import com.marmatsan.figmaDesignSync.data.datasource.modules.ProjectModulesDataSource
import com.marmatsan.figmaDesignSync.data.datasource.versions.RepositoryVersionsDataSource
import com.marmatsan.figmaDesignSync.data.figma.client.FigmaFileContentClient
import com.marmatsan.figmaDesignSync.domain.port.catalog.ProjectCatalogTreesPort
import com.marmatsan.figmaDesignSync.domain.port.modules.ProjectModuleDependenciesPort
import com.marmatsan.figmaDesignSync.domain.port.modules.ProjectModulesPort
import com.marmatsan.figmaDesignSync.domain.port.versions.RepositoryVersionsPort
import com.marmatsan.figmaDesignSync.plugin.checker.sync.FigmaTrunkSyncChecker
import com.marmatsan.figmaDesignSync.plugin.generator.FigmaDesignModelGenerator
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@Component
internal abstract class figmaDesignSyncComponent {
    abstract val designModelGenerator: FigmaDesignModelGenerator
    abstract val trunkSyncChecker: FigmaTrunkSyncChecker

    @Provides
    protected fun figmaFileContentClient(): FigmaFileContentClient {
        return FigmaFileContentClient()
    }

    @Provides
    protected fun repositoryVersionsPort(dataSource: RepositoryVersionsDataSource): RepositoryVersionsPort =
        dataSource

    @Provides
    protected fun projectCatalogTreesPort(dataSource: ProjectCatalogTreesDataSource): ProjectCatalogTreesPort =
        dataSource

    @Provides
    protected fun projectModulesPort(dataSource: ProjectModulesDataSource): ProjectModulesPort =
        dataSource

    @Provides
    protected fun projectModuleDependenciesPort(
        dataSource: ProjectModuleDependenciesDataSource
    ): ProjectModuleDependenciesPort =
        dataSource
}
