package com.marmatsan.figmaDesignSync.plugin.di

import com.marmatsan.figmaDesignSync.data.datasource.catalog.ProjectCatalogTreesDataSource
import com.marmatsan.figmaDesignSync.data.datasource.ci.CiExternalTopologyDataSource
import com.marmatsan.figmaDesignSync.data.datasource.ci.CiWindowsRuntimeDataSource
import com.marmatsan.figmaDesignSync.data.datasource.ci.TeamCityConfigurationDataSource
import com.marmatsan.figmaDesignSync.data.datasource.impact.FigmaChangeImpactPolicyDataSource
import com.marmatsan.figmaDesignSync.data.datasource.impact.GitRepositoryChangeSetDataSource
import com.marmatsan.figmaDesignSync.data.datasource.modules.ProjectModuleDependenciesDataSource
import com.marmatsan.figmaDesignSync.data.datasource.modules.ProjectModulesDataSource
import com.marmatsan.figmaDesignSync.data.datasource.versions.RepositoryVersionsDataSource
import com.marmatsan.figmaDesignSync.data.figma.client.FigmaFileContentClient
import com.marmatsan.figmaDesignSync.data.figma.artifact.OfficialFigmaArtifactSetReader
import com.marmatsan.figmaDesignSync.data.figma.sync.OfficialFigmaSyncScopeJson
import com.marmatsan.figmaDesignSync.domain.service.artifact.OfficialFigmaArtifactContractValidator
import com.marmatsan.figmaDesignSync.domain.port.catalog.ProjectCatalogTreesPort
import com.marmatsan.figmaDesignSync.domain.port.ci.CiExternalTopologyPort
import com.marmatsan.figmaDesignSync.domain.port.ci.CiWindowsRuntimePort
import com.marmatsan.figmaDesignSync.domain.port.ci.TeamCityConfigurationPort
import com.marmatsan.figmaDesignSync.domain.port.impact.FigmaChangeImpactPolicyPort
import com.marmatsan.figmaDesignSync.domain.port.impact.RepositoryChangeSetPort
import com.marmatsan.figmaDesignSync.domain.port.modules.ProjectModuleDependenciesPort
import com.marmatsan.figmaDesignSync.domain.port.modules.ProjectModulesPort
import com.marmatsan.figmaDesignSync.domain.port.versions.RepositoryVersionsPort
import com.marmatsan.figmaDesignSync.plugin.checker.catalog.CatalogUsageChecker
import com.marmatsan.figmaDesignSync.plugin.checker.sync.FigmaTrunkSyncChecker
import com.marmatsan.figmaDesignSync.plugin.checker.impact.FigmaChangeImpactClassifier
import com.marmatsan.figmaDesignSync.plugin.checker.versions.VersionNamingChecker
import com.marmatsan.figmaDesignSync.plugin.generator.FigmaDesignModelGenerator
import com.marmatsan.figmaDesignSync.plugin.checker.ci.CiExternalTopologyFreshnessChecker
import com.marmatsan.figmaDesignSync.plugin.checker.ci.CiWindowsRuntimeFreshnessChecker
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
     * Pure service used by `classifyFigmaChangeImpact`.
     */
    abstract val changeImpactClassifier: FigmaChangeImpactClassifier

    /**
     * Policy adapter used by `classifyFigmaChangeImpact`.
     */
    abstract val changeImpactPolicyPort: FigmaChangeImpactPolicyPort

    /**
     * Git adapter used by `classifyFigmaChangeImpact`.
     */
    abstract val repositoryChangeSetPort: RepositoryChangeSetPort

    /**
     * Service used by `generateFigmaDesignModel`.
     */
    abstract val designModelGenerator: FigmaDesignModelGenerator

    /**
     * Service used by the non-blocking external topology freshness check.
     */
    abstract val ciExternalTopologyFreshnessChecker: CiExternalTopologyFreshnessChecker

    /**
     * Service used by the non-blocking Windows runtime freshness check.
     */
    abstract val ciWindowsRuntimeFreshnessChecker: CiWindowsRuntimeFreshnessChecker

    /**
     * Service used by `checkFigmaTrunkSync`.
     */
    abstract val trunkSyncChecker: FigmaTrunkSyncChecker

    /**
     * Service used by `checkFigmaCatalogUsage`.
     */
    abstract val catalogUsageChecker: CatalogUsageChecker

    /**
     * Service used by `checkFigmaVersionNaming`.
     */
    abstract val versionNamingChecker: VersionNamingChecker

    /** Reads the filesystem artifact set consumed by the MCP operator handoff. */
    abstract val officialFigmaArtifactSetReader: OfficialFigmaArtifactSetReader

    /** Validates the cross-file identity of an official main artifact set. */
    abstract val officialFigmaArtifactContractValidator: OfficialFigmaArtifactContractValidator

    /** Reads and writes the scope shared by official Figma Sync jobs. */
    abstract val officialFigmaSyncScopeJson: OfficialFigmaSyncScopeJson

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
    protected fun ciExternalTopologyPort(dataSource: CiExternalTopologyDataSource): CiExternalTopologyPort =
        dataSource

    @Provides
    protected fun ciWindowsRuntimePort(dataSource: CiWindowsRuntimeDataSource): CiWindowsRuntimePort =
        dataSource

    @Provides
    protected fun teamCityConfigurationPort(dataSource: TeamCityConfigurationDataSource): TeamCityConfigurationPort =
        dataSource

    @Provides
    protected fun projectModulesPort(dataSource: ProjectModulesDataSource): ProjectModulesPort =
        dataSource

    @Provides
    protected fun projectModuleDependenciesPort(
        dataSource: ProjectModuleDependenciesDataSource
    ): ProjectModuleDependenciesPort =
        dataSource

    @Provides
    protected fun changeImpactPolicyPort(
        dataSource: FigmaChangeImpactPolicyDataSource
    ): FigmaChangeImpactPolicyPort = dataSource

    @Provides
    protected fun repositoryChangeSetPort(
        dataSource: GitRepositoryChangeSetDataSource
    ): RepositoryChangeSetPort = dataSource
}
