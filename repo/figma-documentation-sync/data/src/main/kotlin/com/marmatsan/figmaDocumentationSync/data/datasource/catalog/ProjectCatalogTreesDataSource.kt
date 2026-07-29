package com.marmatsan.figmaDocumentationSync.data.datasource.catalog

import com.marmatsan.figmaDocumentationSync.data.dependencies.catalog.DependencyDslCatalogProviderFactory
import com.marmatsan.figmaDocumentationSync.data.gradle.catalog.GradleConventionPluginTreeReader
import com.marmatsan.figmaDocumentationSync.data.gradle.catalog.GradleIncludedBuildCatalogUsageReader
import com.marmatsan.figmaDocumentationSync.data.gradle.catalog.GradleMainCatalogUsageReader
import com.marmatsan.figmaDocumentationSync.data.gradle.catalog.GradlePluginTreeReader
import com.marmatsan.figmaDocumentationSync.data.gradle.catalog.IncludedBuildSettingsCatalogReader
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogTree
import com.marmatsan.figmaDocumentationSync.domain.port.catalog.ProjectCatalogTreeSource
import com.marmatsan.figmaDocumentationSync.domain.port.catalog.ProjectCatalogTreesPort
import me.tatarka.inject.annotations.Inject
import java.io.File

/**
 * Adapter that implements [ProjectCatalogTreesPort] by delegating each
 * [ProjectCatalogTreeSource] variant to the reader that understands that
 * repository source.
 *
 * This class is where file paths from the domain source objects are converted
 * back into [File] instances. Keeping that conversion here preserves the
 * domain module's IO-free boundary.
 *
 * @see IncludedBuildSettingsCatalogReader
 * @see GradleConventionPluginTreeReader
 * @see GradlePluginTreeReader
 */
@Inject
class ProjectCatalogTreesDataSource(
    private val includedBuildSettingsCatalogReader: IncludedBuildSettingsCatalogReader,
    private val includedBuildCatalogUsageReader: GradleIncludedBuildCatalogUsageReader,
    private val mainCatalogUsageReader: GradleMainCatalogUsageReader,
    private val gradleConventionPluginTreeReader: GradleConventionPluginTreeReader,
    private val gradlePluginTreeReader: GradlePluginTreeReader
) : ProjectCatalogTreesPort {
    /**
     * Reads library trees only from source variants that define libraries.
     */
    override fun readLibraryTree(
        source: ProjectCatalogTreeSource
    ): LibraryCatalogTree =
        when (source) {
            is ProjectCatalogTreeSource.DependenciesDslVersionAliases -> {
                DependencyDslCatalogProviderFactory
                    .create(source.providerClassName)
                    .readLibraryTreeWithVersionAliases(
                        rootDirPath = source.rootDirPath,
                        conventionPluginIncludedBuilds = source.conventionPluginIncludedBuilds
                    )
            }

            is ProjectCatalogTreeSource.IncludedBuildSettings -> {
                includedBuildSettingsCatalogReader.readLibraryTree(
                    settingsFile = File(source.includedBuild.settingsFilePath),
                    usageByAlias =
                        includedBuildCatalogUsageReader.readLibraryUsages(
                            rootDir = File(source.includedBuild.rootDirPath),
                            modulePathPrefix = source.includedBuild.modulePathPrefix
                        )
                )
            }

            is ProjectCatalogTreeSource.CustomGradleConventionPlugins -> {
                error("Custom Gradle convention plugins do not define a library catalog tree")
            }

            is ProjectCatalogTreeSource.CustomGradlePlugins -> {
                error("Custom Gradle plugins do not define a library catalog tree")
            }
        }

    /**
     * Reads plugin trees from dependency catalogs, gradle-plugins settings, and
     * repository-owned Gradle plugin declarations.
     */
    override fun readPluginTree(
        source: ProjectCatalogTreeSource
    ): PluginCatalogTree =
        when (source) {
            is ProjectCatalogTreeSource.DependenciesDslVersionAliases -> {
                DependencyDslCatalogProviderFactory
                    .create(source.providerClassName)
                    .readPluginTreeWithVersionAliases(
                        rootDirPath = source.rootDirPath,
                        conventionPluginIncludedBuilds = source.conventionPluginIncludedBuilds
                    )
            }

            is ProjectCatalogTreeSource.IncludedBuildSettings -> {
                includedBuildSettingsCatalogReader.readPluginTree(
                    settingsFile = File(source.includedBuild.settingsFilePath),
                    usageByAlias =
                        includedBuildCatalogUsageReader.readPluginUsages(
                            rootDir = File(source.includedBuild.rootDirPath),
                            modulePathPrefix = source.includedBuild.modulePathPrefix
                        )
                )
            }

            is ProjectCatalogTreeSource.CustomGradleConventionPlugins -> {
                source.includedBuilds
                    .filter { includedBuild -> includedBuild.publishesConventionPlugins }
                    .map { includedBuild ->
                        gradleConventionPluginTreeReader.readPluginTree(
                            rootDir = File(includedBuild.rootDirPath),
                            usageByPluginId =
                                mainCatalogUsageReader.readLiteralPluginUsages(
                                    rootDir = File(source.rootDirPath)
                                )
                        )
                    }.mergePluginTrees()
            }

            is ProjectCatalogTreeSource.CustomGradlePlugins -> {
                gradlePluginTreeReader.readPluginTree(
                    rootDir = File(source.rootDirPath),
                    includedPluginIds =
                        mainCatalogUsageReader.readAppliedLiteralPluginIds(
                            rootDir = File(source.rootDirPath)
                        ),
                    usageByPluginId =
                        mainCatalogUsageReader.readAppliedLiteralPluginUsages(
                            rootDir = File(source.rootDirPath)
                        )
                )
            }
        }
}

private fun List<PluginCatalogTree>.mergePluginTrees(): PluginCatalogTree =
    fold(
        PluginCatalogTree(
            roots = emptyList()
        )
    ) { mergedTree, tree ->
        mergedTree.merge(
            other = tree
        )
    }

private fun PluginCatalogTree.merge(
    other: PluginCatalogTree
): PluginCatalogTree =
    copy(
        roots =
            roots.mergePluginNodes(
                other = other.roots
            )
    )

private fun List<PluginCatalogNode>.mergePluginNodes(
    other: List<PluginCatalogNode>
): List<PluginCatalogNode> =
    (this + other)
        .groupBy(PluginCatalogNode::id)
        .map { (_, nodes) -> nodes.reduce(PluginCatalogNode::merge) }
        .sortedBy(PluginCatalogNode::id)

private fun PluginCatalogNode.merge(
    other: PluginCatalogNode
): PluginCatalogNode =
    copy(
        version = version ?: other.version,
        appliedToModules = (appliedToModules + other.appliedToModules).sorted(),
        providedByConventionPlugins =
            (providedByConventionPlugins + other.providedByConventionPlugins)
                .distinct()
                .sortedWith(
                    compareBy(
                        PluginCatalogNode.ConventionPluginUsage::pluginId,
                        PluginCatalogNode.ConventionPluginUsage::pluginModule
                    )
                ),
        children =
            children.mergePluginNodes(
                other = other.children
            )
    )
