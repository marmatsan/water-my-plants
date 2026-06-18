package com.marmatsan.figmaCatalogChecks.data

import com.marmatsan.figmaCatalogChecks.domain.FigmaCatalogTreeSource
import com.marmatsan.figmaCatalogChecks.domain.FigmaCatalogTreesPort
import com.marmatsan.figmaCatalogChecks.domain.FigmaModuleComponentSource
import com.marmatsan.figmaCatalogChecks.domain.FigmaModulesPort
import com.marmatsan.figmaCatalogChecks.domain.FigmaVersionsPort
import com.marmatsan.figmaCatalogChecks.domain.FigmaVersionsSource
import com.marmatsan.figmaCatalogChecks.domain.LibraryCatalogTree
import com.marmatsan.figmaCatalogChecks.domain.PluginCatalogTree
import com.marmatsan.figmaCatalogChecks.domain.ProjectCatalogTreeSource
import com.marmatsan.figmaCatalogChecks.domain.ProjectCatalogTreesPort
import com.marmatsan.figmaCatalogChecks.domain.ProjectModulesPort
import com.marmatsan.figmaCatalogChecks.domain.ProjectModulesSource
import com.marmatsan.figmaCatalogChecks.domain.RepositoryVersionsPort
import com.marmatsan.figmaCatalogChecks.domain.VersionsFileSource
import me.tatarka.inject.annotations.Inject
import java.io.File

@Inject
class RepositoryVersionsDataSource(
    private val versionsPropertiesReader: VersionsPropertiesReader
) : RepositoryVersionsPort {
    override fun readVersions(source: VersionsFileSource): Map<String, String> =
        versionsPropertiesReader.read(File(source.path))
}

@Inject
class FigmaVersionsDataSource(
    private val figmaFileContentClient: FigmaFileContentClient,
    private val figmaFileVersionsReader: FigmaFileVersionsReader
) : FigmaVersionsPort {
    override fun readVersions(source: FigmaVersionsSource): Map<String, String> =
        figmaFileVersionsReader.readSection(
            section = figmaFileContentClient.getNodeContent(
                fileKey = source.section.fileKey,
                token = source.token,
                nodeId = source.section.nodeId
            ),
            sectionNodeId = source.section.nodeId,
            versionComponentNodeId = source.versionComponent.nodeId
        )
}

@Inject
class ProjectCatalogTreesDataSource(
    private val buildLogicSettingsCatalogReader: BuildLogicSettingsCatalogReader,
    private val dependenciesCatalogTreesReader: DependenciesCatalogTreesReader
) : ProjectCatalogTreesPort {
    override fun readLibraryTree(source: ProjectCatalogTreeSource): LibraryCatalogTree =
        when (source) {
            ProjectCatalogTreeSource.DependenciesDslVersionAliases ->
                dependenciesCatalogTreesReader.readLibraryTreeWithVersionAliases()

            is ProjectCatalogTreeSource.BuildLogicSettings ->
                buildLogicSettingsCatalogReader.readLibraryTree(File(source.settingsFilePath))
        }

    override fun readPluginTree(source: ProjectCatalogTreeSource): PluginCatalogTree =
        when (source) {
            ProjectCatalogTreeSource.DependenciesDslVersionAliases ->
                dependenciesCatalogTreesReader.readPluginTreeWithVersionAliases()

            is ProjectCatalogTreeSource.BuildLogicSettings ->
                buildLogicSettingsCatalogReader.readPluginTree(File(source.settingsFilePath))
        }
}

@Inject
class FigmaCatalogTreesDataSource(
    private val figmaFileContentClient: FigmaFileContentClient,
    private val figmaFileLibraryCatalogTreeReader: FigmaFileLibraryCatalogTreeReader,
    private val figmaFilePluginCatalogTreeReader: FigmaFilePluginCatalogTreeReader
) : FigmaCatalogTreesPort {
    override fun readLibraryTree(source: FigmaCatalogTreeSource): LibraryCatalogTree =
        figmaFileLibraryCatalogTreeReader.readSection(
            section = figmaFileContentClient.getNodeContent(
                fileKey = source.section.fileKey,
                token = source.token,
                nodeId = source.section.nodeId
            ),
            sectionNodeId = source.section.nodeId
        )

    override fun readPluginTree(source: FigmaCatalogTreeSource): PluginCatalogTree =
        figmaFilePluginCatalogTreeReader.readSection(
            section = figmaFileContentClient.getNodeContent(
                fileKey = source.section.fileKey,
                token = source.token,
                nodeId = source.section.nodeId
            ),
            sectionNodeId = source.section.nodeId
        )
}

@Inject
class ProjectModulesDataSource(
    private val gradleProjectModulesReader: GradleProjectModulesReader
) : ProjectModulesPort {
    override fun readModules(source: ProjectModulesSource): Set<String> =
        gradleProjectModulesReader.readModules(
            rootSettingsFile = File(source.rootSettingsFilePath),
            buildLogicSettingsFile = File(source.buildLogicSettingsFilePath)
        )
}

@Inject
class FigmaModulesDataSource(
    private val figmaFileContentClient: FigmaFileContentClient,
    private val figmaModuleComponentReader: FigmaModuleComponentReader
) : FigmaModulesPort {
    override fun readModules(source: FigmaModuleComponentSource): Set<String> =
        figmaModuleComponentReader.readComponent(
            component = figmaFileContentClient.getNodeContent(
                fileKey = source.component.fileKey,
                token = source.token,
                nodeId = source.component.nodeId
            ),
            componentNodeId = source.component.nodeId
        )
}
