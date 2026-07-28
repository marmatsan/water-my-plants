package com.marmatsan.figmaDocumentationSync.plugin.di

import com.marmatsan.figmaDocumentationSync.data.datasource.catalog.ProjectCatalogTreesDataSource
import com.marmatsan.figmaDocumentationSync.data.datasource.ci.CiConfigurationDataSource
import com.marmatsan.figmaDocumentationSync.data.datasource.ci.CiExternalTopologyDataSource
import com.marmatsan.figmaDocumentationSync.data.datasource.ci.CiWindowsRuntimeDataSource
import com.marmatsan.figmaDocumentationSync.data.datasource.impact.FigmaChangeImpactPolicyDataSource
import com.marmatsan.figmaDocumentationSync.data.datasource.impact.GitRepositoryChangeSetDataSource
import com.marmatsan.figmaDocumentationSync.data.datasource.modules.ProjectModuleDependenciesDataSource
import com.marmatsan.figmaDocumentationSync.data.datasource.modules.ProjectModulesDataSource
import com.marmatsan.figmaDocumentationSync.data.datasource.versions.RepositoryVersionsDataSource
import com.marmatsan.figmaDocumentationSync.data.figma.artifact.CanonicalFigmaArtifactSetReader
import com.marmatsan.figmaDocumentationSync.data.figma.client.FigmaFileContentClient
import com.marmatsan.figmaDocumentationSync.data.figma.sync.CanonicalFigmaSyncScopeJson
import com.marmatsan.figmaDocumentationSync.domain.port.catalog.ProjectCatalogTreesPort
import com.marmatsan.figmaDocumentationSync.domain.port.ci.CiConfigurationPort
import com.marmatsan.figmaDocumentationSync.domain.port.ci.CiExternalTopologyPort
import com.marmatsan.figmaDocumentationSync.domain.port.ci.CiWindowsRuntimePort
import com.marmatsan.figmaDocumentationSync.domain.port.figma.FigmaNodeContentSource
import com.marmatsan.figmaDocumentationSync.domain.port.impact.FigmaChangeImpactPolicyPort
import com.marmatsan.figmaDocumentationSync.domain.port.impact.RepositoryChangeSetPort
import com.marmatsan.figmaDocumentationSync.domain.port.modules.ProjectModuleDependenciesPort
import com.marmatsan.figmaDocumentationSync.domain.port.modules.ProjectModulesPort
import com.marmatsan.figmaDocumentationSync.domain.port.versions.RepositoryVersionsPort
import com.marmatsan.figmaDocumentationSync.domain.service.artifact.CanonicalFigmaArtifactContractValidator
import com.marmatsan.figmaDocumentationSync.plugin.checker.catalog.CatalogUsageChecker
import com.marmatsan.figmaDocumentationSync.plugin.checker.ci.CiExternalTopologyFreshnessChecker
import com.marmatsan.figmaDocumentationSync.plugin.checker.ci.CiWindowsRuntimeFreshnessChecker
import com.marmatsan.figmaDocumentationSync.plugin.checker.impact.FigmaChangeImpactClassifier
import com.marmatsan.figmaDocumentationSync.plugin.checker.sync.FigmaTrunkSyncChecker
import com.marmatsan.figmaDocumentationSync.plugin.checker.versions.VersionNamingChecker
import com.marmatsan.figmaDocumentationSync.plugin.generator.FigmaDesignModelGenerator
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
internal abstract class FigmaDocumentationSyncComponent {
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
    abstract val canonicalFigmaArtifactSetReader: CanonicalFigmaArtifactSetReader

    /** Validates the cross-file identity of a canonical main artifact set. */
    abstract val canonicalFigmaArtifactContractValidator: CanonicalFigmaArtifactContractValidator

    /** Reads and writes the scope shared by canonical Figma Sync jobs. */
    abstract val canonicalFigmaSyncScopeJson: CanonicalFigmaSyncScopeJson

    /** Reads the narrow Figma node-content contract used by sync decisions. */
    abstract val figmaNodeContentSource: FigmaNodeContentSource

    /**
     * Provides the narrow Figma API client used only by the sync checker.
     */
    @Provides
    protected fun figmaNodeContentSource(): FigmaNodeContentSource = FigmaFileContentClient()

    @Provides
    protected fun repositoryVersionsPort(
        dataSource: RepositoryVersionsDataSource,
    ): RepositoryVersionsPort =
        dataSource

    @Provides
    protected fun projectCatalogTreesPort(
        dataSource: ProjectCatalogTreesDataSource,
    ): ProjectCatalogTreesPort =
        dataSource

    @Provides
    protected fun ciExternalTopologyPort(
        dataSource: CiExternalTopologyDataSource,
    ): CiExternalTopologyPort =
        dataSource

    @Provides
    protected fun ciWindowsRuntimePort(
        dataSource: CiWindowsRuntimeDataSource,
    ): CiWindowsRuntimePort =
        dataSource

    @Provides
    protected fun ciConfigurationPort(
        dataSource: CiConfigurationDataSource,
    ): CiConfigurationPort =
        dataSource

    @Provides
    protected fun projectModulesPort(
        dataSource: ProjectModulesDataSource,
    ): ProjectModulesPort =
        dataSource

    @Provides
    protected fun projectModuleDependenciesPort(
        dataSource: ProjectModuleDependenciesDataSource,
    ): ProjectModuleDependenciesPort =
        dataSource

    @Provides
    protected fun changeImpactPolicyPort(
        dataSource: FigmaChangeImpactPolicyDataSource,
    ): FigmaChangeImpactPolicyPort = dataSource

    @Provides
    protected fun repositoryChangeSetPort(
        dataSource: GitRepositoryChangeSetDataSource,
    ): RepositoryChangeSetPort = dataSource
}
