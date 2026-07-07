package com.marmatsan.figmaDesignSync.data.datasource.catalog

import com.marmatsan.figmaDesignSync.data.dependencies.catalog.DependenciesCatalogTreesReader
import com.marmatsan.figmaDesignSync.data.gradle.catalog.GradleCatalogUsageReader
import com.marmatsan.figmaDesignSync.data.gradle.catalog.GradleConventionPluginTreeReader
import com.marmatsan.figmaDesignSync.data.gradle.catalog.GradlePluginTreeReader
import com.marmatsan.figmaDesignSync.data.gradle.catalog.IncludedBuildSettingsCatalogReader
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaDesignSync.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaDesignSync.domain.model.catalog.PluginCatalogTree
import com.marmatsan.figmaDesignSync.domain.port.catalog.ProjectCatalogTreeSource
import com.marmatsan.figmaDesignSync.domain.port.catalog.ProjectCatalogTreesPort
import java.io.File
import me.tatarka.inject.annotations.Inject

/**
 * Adapter that implements [ProjectCatalogTreesPort] by delegating each
 * [ProjectCatalogTreeSource] variant to the reader that understands that
 * repository source.
 *
 * This class is where file paths from the domain source objects are converted
 * back into [File] instances. Keeping that conversion here preserves the
 * domain module's IO-free boundary.
 *
 * @see DependenciesCatalogTreesReader
 * @see IncludedBuildSettingsCatalogReader
 * @see GradleConventionPluginTreeReader
 * @see GradlePluginTreeReader
 */
@Inject
class ProjectCatalogTreesDataSource(
    private val includedBuildSettingsCatalogReader: IncludedBuildSettingsCatalogReader,
    private val dependenciesCatalogTreesReader: DependenciesCatalogTreesReader,
    private val gradleCatalogUsageReader: GradleCatalogUsageReader,
    private val gradleConventionPluginTreeReader: GradleConventionPluginTreeReader,
    private val gradlePluginTreeReader: GradlePluginTreeReader
) : ProjectCatalogTreesPort {
    /**
     * Reads library trees only from source variants that define libraries.
     */
    override fun readLibraryTree(source: ProjectCatalogTreeSource): LibraryCatalogTree =
        when (source) {
            is ProjectCatalogTreeSource.DependenciesDslVersionAliases ->
                dependenciesCatalogTreesReader.readLibraryTreeWithVersionAliases(
                    rootDir = File(source.rootDirPath)
                )

            is ProjectCatalogTreeSource.IncludedBuildSettings ->
                includedBuildSettingsCatalogReader.readLibraryTree(
                    settingsFile = File(source.includedBuild.settingsFilePath),
                    usageByAlias = gradleCatalogUsageReader.readIncludedBuildLibraryUsages(
                        rootDir = File(source.includedBuild.rootDirPath),
                        modulePathPrefix = source.includedBuild.modulePathPrefix
                    )
                )

            is ProjectCatalogTreeSource.CustomGradleConventionPlugins ->
                error("Custom Gradle convention plugins do not define a library catalog tree")

            is ProjectCatalogTreeSource.CustomGradlePlugins ->
                error("Custom Gradle plugins do not define a library catalog tree")
        }

    /**
     * Reads plugin trees from dependency catalogs, gradle-plugins settings, and
     * repository-owned Gradle plugin declarations.
     */
    override fun readPluginTree(source: ProjectCatalogTreeSource): PluginCatalogTree =
        when (source) {
            is ProjectCatalogTreeSource.DependenciesDslVersionAliases ->
                dependenciesCatalogTreesReader.readPluginTreeWithVersionAliases(
                    rootDir = File(source.rootDirPath)
                )

            is ProjectCatalogTreeSource.IncludedBuildSettings ->
                includedBuildSettingsCatalogReader.readPluginTree(
                    settingsFile = File(source.includedBuild.settingsFilePath),
                    usageByAlias = gradleCatalogUsageReader.readIncludedBuildPluginUsages(
                        rootDir = File(source.includedBuild.rootDirPath),
                        modulePathPrefix = source.includedBuild.modulePathPrefix
                    )
                )

            is ProjectCatalogTreeSource.CustomGradleConventionPlugins ->
                source.includedBuilds
                    .filter { includedBuild -> includedBuild.publishesConventionPlugins }
                    .map { includedBuild ->
                        gradleConventionPluginTreeReader.readPluginTree(
                            rootDir = File(includedBuild.rootDirPath),
                            usageByPluginId = gradleCatalogUsageReader.readMainLiteralPluginUsages(
                                rootDir = File(source.rootDirPath)
                            )
                        )
                    }
                    .mergePluginTrees()

            is ProjectCatalogTreeSource.CustomGradlePlugins ->
                gradlePluginTreeReader.readPluginTree(
                    rootDir = File(source.rootDirPath),
                    includedPluginIds = gradleCatalogUsageReader.readMainAppliedLiteralPluginIds(
                        rootDir = File(source.rootDirPath)
                    ),
                    usageByPluginId = gradleCatalogUsageReader.readMainAppliedLiteralPluginUsages(
                        rootDir = File(source.rootDirPath)
                    )
                )
        }
}

private fun List<PluginCatalogTree>.mergePluginTrees(): PluginCatalogTree =
    fold(PluginCatalogTree(roots = emptyList())) { mergedTree, tree -> mergedTree.merge(tree) }

private fun PluginCatalogTree.merge(other: PluginCatalogTree): PluginCatalogTree =
    copy(
        roots = roots.mergePluginNodes(other.roots)
    )

private fun List<PluginCatalogNode>.mergePluginNodes(other: List<PluginCatalogNode>): List<PluginCatalogNode> =
    (this + other)
        .groupBy(PluginCatalogNode::id)
        .map { (_, nodes) -> nodes.reduce(PluginCatalogNode::merge) }
        .sortedBy(PluginCatalogNode::id)

private fun PluginCatalogNode.merge(other: PluginCatalogNode): PluginCatalogNode =
    copy(
        version = version ?: other.version,
        appliedToModules = (appliedToModules + other.appliedToModules).sorted(),
        children = children.mergePluginNodes(other.children)
    )
