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
import com.marmatsan.figmaDesignSync.plugin.checker.catalog.CatalogUsageChecker
import com.marmatsan.figmaDesignSync.plugin.checker.sync.FigmaTrunkSyncChecker
import com.marmatsan.figmaDesignSync.plugin.generator.FigmaDesignModelGenerator
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

/**
 * kotlin-inject composition root for the Figma design sync Gradle tasks.
 *
 * The component wires data-layer adapters into domain ports and exposes the two
 * application services used by Gradle tasks: [designModelGenerator] and
 * [trunkSyncChecker].
 */
@Component
internal abstract class figmaDesignSyncComponent {
    /**
     * Service used by `generateFigmaDesignModel`.
     */
    abstract val designModelGenerator: FigmaDesignModelGenerator

    /**
     * Service used by `checkFigmaTrunkSync`.
     */
    abstract val trunkSyncChecker: FigmaTrunkSyncChecker

    /**
     * Service used by `checkFigmaCatalogUsage`.
     */
    abstract val catalogUsageChecker: CatalogUsageChecker

    /**
     * Provides the narrow Figma API client used only by the sync checker.
     */
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
