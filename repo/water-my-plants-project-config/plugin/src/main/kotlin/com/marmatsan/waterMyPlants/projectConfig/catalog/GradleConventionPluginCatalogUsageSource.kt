package com.marmatsan.waterMyPlants.projectConfig.catalog

import com.marmatsan.figmaDocumentationSync.data.gradle.catalog.GradleConventionCatalogUsageReader
import com.marmatsan.figmaDocumentationSync.data.gradle.catalog.GradleMainCatalogUsageReader
import com.marmatsan.figmaDocumentationSync.data.gradle.catalog.LibraryConfigurationUsages
import com.marmatsan.figmaDocumentationSync.data.gradle.catalog.LibraryUsages
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogEntry.ConventionPluginConfigurationUsage
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogEntry.ConventionPluginUsage
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaDocumentationSync.domain.port.gradle.IncludedBuildSource
import java.io.File

/** Reads and normalizes catalog usage contributed by convention-plugin builds. */
internal class GradleConventionPluginCatalogUsageSource(
    private val reader: GradleConventionCatalogUsageReader,
    private val mainReader: GradleMainCatalogUsageReader
) : ConventionPluginCatalogUsageSource {
    /** Aggregates library usage from included builds that publish convention plugins. */
    override fun libraryUsages(
        rootDir: File,
        includedBuilds: List<IncludedBuildSource>
    ): ConventionPluginLibraryUsages {
        val modulesByPluginId =
            mainReader.readAppliedLiteralPluginUsages(
                rootDir = rootDir
            )

        return includedBuilds
            .filter(IncludedBuildSource::publishesConventionPlugins)
            .fold(ConventionPluginLibraryUsages()) { usages, includedBuild ->
                val includedBuildRootDir = File(includedBuild.rootDirPath)
                val pluginIdsByModule =
                    reader.readPluginIdsByModule(
                        rootDir = includedBuildRootDir,
                        modulePathPrefix = includedBuild.modulePathPrefix
                    )
                usages.merge(
                    other =
                        reader
                            .readLibraryUsages(
                                rootDir = includedBuildRootDir,
                                modulePathPrefix = includedBuild.modulePathPrefix
                            ).toConventionPluginLibraryUsages(
                                pluginIdsByModule = pluginIdsByModule,
                                modulesByPluginId = modulesByPluginId
                            ).merge(
                                other =
                                    reader
                                        .readLibraryConfigurationUsages(
                                            rootDir = includedBuildRootDir,
                                            modulePathPrefix = includedBuild.modulePathPrefix
                                        ).toConventionPluginLibraryConfigurationUsages(
                                            pluginIdsByModule = pluginIdsByModule
                                        )
                            )
                )
            }
    }

    /** Aggregates plugin usage from included builds that publish convention plugins. */
    override fun pluginUsages(
        rootDir: File,
        includedBuilds: List<IncludedBuildSource>
    ): Map<String, List<PluginCatalogNode.ConventionPluginUsage>> {
        val modulesByPluginId =
            mainReader.readAppliedLiteralPluginUsages(
                rootDir = rootDir
            )

        return includedBuilds
            .filter(IncludedBuildSource::publishesConventionPlugins)
            .fold(emptyMap()) { usages, includedBuild ->
                val includedBuildRootDir = File(includedBuild.rootDirPath)
                val pluginIdsByModule =
                    reader.readPluginIdsByModule(
                        rootDir = includedBuildRootDir,
                        modulePathPrefix = includedBuild.modulePathPrefix
                    )
                usages.mergePluginUsages(
                    other =
                        reader
                            .readPluginUsages(
                                rootDir = includedBuildRootDir,
                                modulePathPrefix = includedBuild.modulePathPrefix
                            ).toConventionPluginPluginUsages(
                                pluginIdsByModule = pluginIdsByModule,
                                modulesByPluginId = modulesByPluginId
                            )
                )
            }
    }
}

private fun LibraryUsages.toConventionPluginLibraryUsages(
    pluginIdsByModule: Map<String, Set<String>>,
    modulesByPluginId: Map<String, Set<String>>
): ConventionPluginLibraryUsages =
    ConventionPluginLibraryUsages(
        coordinates =
            coordinates.toConventionPluginUsageMap(
                pluginIdsByModule = pluginIdsByModule,
                modulesByPluginId = modulesByPluginId
            ),
        bundles =
            bundles.toConventionPluginUsageMap(
                pluginIdsByModule = pluginIdsByModule,
                modulesByPluginId = modulesByPluginId
            )
    )

private fun Map<String, Set<String>>.toConventionPluginUsageMap(
    pluginIdsByModule: Map<String, Set<String>>,
    modulesByPluginId: Map<String, Set<String>>
): Map<String, List<ConventionPluginUsage>> =
    mapValues { (_, pluginModules) ->
        pluginModules
            .flatMap { pluginModule ->
                pluginIdsByModule[pluginModule].orEmpty().map { pluginId ->
                    ConventionPluginUsage(
                        pluginId = pluginId,
                        pluginModule = pluginModule,
                        requiredByModules = modulesByPluginId[pluginId].orEmpty().sorted()
                    )
                }
            }.distinct()
            .sortedWith(
                compareBy(
                    ConventionPluginUsage::pluginId,
                    ConventionPluginUsage::pluginModule
                )
            )
    }.filterValues(List<ConventionPluginUsage>::isNotEmpty)

private fun LibraryConfigurationUsages.toConventionPluginLibraryConfigurationUsages(
    pluginIdsByModule: Map<String, Set<String>>
): ConventionPluginLibraryUsages =
    ConventionPluginLibraryUsages(
        configuredCoordinates =
            coordinates
                .mapValues { (_, usages) ->
                    usages
                        .flatMap { usage ->
                            pluginIdsByModule[usage.pluginModule].orEmpty().map { pluginId ->
                                ConventionPluginConfigurationUsage(
                                    pluginId = pluginId,
                                    pluginModule = usage.pluginModule,
                                    target = usage.target
                                )
                            }
                        }.distinct()
                        .sortedWith(
                            compareBy(
                                ConventionPluginConfigurationUsage::pluginId,
                                ConventionPluginConfigurationUsage::pluginModule,
                                ConventionPluginConfigurationUsage::target
                            )
                        )
                }.filterValues(List<ConventionPluginConfigurationUsage>::isNotEmpty)
    )

private fun Map<String, Set<String>>.toConventionPluginPluginUsages(
    pluginIdsByModule: Map<String, Set<String>>,
    modulesByPluginId: Map<String, Set<String>>
): Map<String, List<PluginCatalogNode.ConventionPluginUsage>> =
    mapValues { (_, pluginModules) ->
        pluginModules
            .flatMap { pluginModule ->
                pluginIdsByModule[pluginModule].orEmpty().map { pluginId ->
                    PluginCatalogNode.ConventionPluginUsage(
                        pluginId = pluginId,
                        pluginModule = pluginModule,
                        requiredByModules = modulesByPluginId[pluginId].orEmpty().sorted()
                    )
                }
            }.distinct()
            .sortedWith(
                compareBy(
                    PluginCatalogNode.ConventionPluginUsage::pluginId,
                    PluginCatalogNode.ConventionPluginUsage::pluginModule
                )
            )
    }.filterValues(List<PluginCatalogNode.ConventionPluginUsage>::isNotEmpty)

private fun ConventionPluginLibraryUsages.merge(
    other: ConventionPluginLibraryUsages
): ConventionPluginLibraryUsages =
    ConventionPluginLibraryUsages(
        coordinates = coordinates.mergeLibraryUsages(other.coordinates),
        bundles = bundles.mergeLibraryUsages(other.bundles),
        configuredCoordinates = configuredCoordinates.mergeConfigurationUsages(other.configuredCoordinates)
    )

private fun Map<String, List<ConventionPluginUsage>>.mergeLibraryUsages(
    other: Map<String, List<ConventionPluginUsage>>
): Map<String, List<ConventionPluginUsage>> =
    mergeLists(
        other = other,
        comparator =
            compareBy(
                ConventionPluginUsage::pluginId,
                ConventionPluginUsage::pluginModule
            )
    )

private fun Map<String, List<ConventionPluginConfigurationUsage>>.mergeConfigurationUsages(
    other: Map<String, List<ConventionPluginConfigurationUsage>>
): Map<String, List<ConventionPluginConfigurationUsage>> =
    mergeLists(
        other = other,
        comparator =
            compareBy(
                ConventionPluginConfigurationUsage::pluginId,
                ConventionPluginConfigurationUsage::pluginModule,
                ConventionPluginConfigurationUsage::target
            )
    )

private fun Map<String, List<PluginCatalogNode.ConventionPluginUsage>>.mergePluginUsages(
    other: Map<String, List<PluginCatalogNode.ConventionPluginUsage>>
): Map<String, List<PluginCatalogNode.ConventionPluginUsage>> =
    mergeLists(
        other = other,
        comparator =
            compareBy(
                PluginCatalogNode.ConventionPluginUsage::pluginId,
                PluginCatalogNode.ConventionPluginUsage::pluginModule
            )
    )

private fun <T> Map<String, List<T>>.mergeLists(
    other: Map<String, List<T>>,
    comparator: Comparator<T>
): Map<String, List<T>> =
    (keys + other.keys).associateWith { key ->
        (this[key].orEmpty() + other[key].orEmpty())
            .distinct()
            .sortedWith(comparator)
    }
