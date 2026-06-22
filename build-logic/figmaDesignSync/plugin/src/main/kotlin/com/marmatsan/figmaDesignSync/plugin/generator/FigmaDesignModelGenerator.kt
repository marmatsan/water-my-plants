package com.marmatsan.figmaDesignSync.plugin.generator

import com.marmatsan.figmaDesignSync.domain.port.catalog.ProjectCatalogTreeSource
import com.marmatsan.figmaDesignSync.domain.port.catalog.ProjectCatalogTreesPort
import com.marmatsan.figmaDesignSync.domain.port.modules.ProjectModuleDependenciesPort
import com.marmatsan.figmaDesignSync.domain.port.modules.ProjectModuleDependenciesScope
import com.marmatsan.figmaDesignSync.domain.port.modules.ProjectModuleDependenciesSource
import com.marmatsan.figmaDesignSync.domain.port.modules.ProjectModulesPort
import com.marmatsan.figmaDesignSync.domain.port.modules.ProjectModulesSource
import com.marmatsan.figmaDesignSync.domain.port.versions.RepositoryVersionsPort
import com.marmatsan.figmaDesignSync.domain.port.versions.VersionsFileSource
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import me.tatarka.inject.annotations.Inject

@Inject
internal class FigmaDesignModelGenerator(
    private val repositoryVersionsPort: RepositoryVersionsPort,
    private val projectCatalogTreesPort: ProjectCatalogTreesPort,
    private val projectModulesPort: ProjectModulesPort,
    private val projectModuleDependenciesPort: ProjectModuleDependenciesPort
) {
    fun generate(request: FigmaDesignModelGenerationRequest): FigmaDesignModelGenerationResult {
        val content = buildContent(request)
        val hashInput = buildJsonObject {
            put("schemaVersion", SCHEMA_VERSION)
            put("branch", request.branch)
            put("gitSha", request.gitSha)
            put("content", content)
        }
        val modelHash = FigmaDesignModelHash.compute(hashInput)
        val model = buildJsonObject {
            put("schemaVersion", SCHEMA_VERSION)
            put("branch", request.branch)
            put("gitSha", request.gitSha)
            put("generatedAt", request.generatedAt.toString())
            put("content", content)
            put("modelHash", modelHash)
        }

        return FigmaDesignModelGenerationResult(
            model = model,
            modelHash = modelHash
        )
    }

    private fun buildContent(request: FigmaDesignModelGenerationRequest) =
        buildJsonObject {
            val versionSections = repositoryVersionsPort
                .readVersionSections(VersionsFileSource(request.versionsFile.absolutePath))
            put(
                "versions",
                versionSections
                    .flatMap { section -> section.versions.entries }
                    .associate { entry -> entry.key to entry.value }
                    .toVersionsJson()
            )
            put("versionSections", versionSections.toVersionSectionsJson())
            put("catalogs", buildCatalogs(request))
            put(
                "modules",
                projectModulesPort
                    .readModules(
                        ProjectModulesSource(
                            rootSettingsFilePath = request.rootSettingsFile.absolutePath,
                            buildLogicSettingsFilePath = request.buildLogicSettingsFile.absolutePath
                        )
                    )
                    .toSortedJsonArray()
            )
            put("moduleDependencies", buildModuleDependencies(request))
        }

    private fun buildCatalogs(request: FigmaDesignModelGenerationRequest) =
        buildJsonObject {
            put(
                "waterMyPlants",
                buildJsonObject {
                    put(
                        "libraries",
                        projectCatalogTreesPort
                            .readLibraryTree(
                                ProjectCatalogTreeSource.DependenciesDslVersionAliases(
                                    rootDirPath = request.projectRootDirectory.absolutePath
                                )
                            )
                            .toDesignJson()
                    )
                    put(
                        "plugins",
                        projectCatalogTreesPort
                            .readPluginTree(
                                ProjectCatalogTreeSource.DependenciesDslVersionAliases(
                                    rootDirPath = request.projectRootDirectory.absolutePath
                                )
                            )
                            .toDesignJson()
                    )
                    put(
                        "customGradleConventionPlugins",
                        projectCatalogTreesPort
                            .readPluginTree(
                                ProjectCatalogTreeSource.CustomGradleConventionPlugins(
                                    rootDirPath = request.projectRootDirectory.absolutePath
                                )
                            )
                            .toDesignJson()
                    )
                    put(
                        "customGradlePlugins",
                        projectCatalogTreesPort
                            .readPluginTree(
                                ProjectCatalogTreeSource.CustomGradlePlugins(
                                    rootDirPath = request.projectRootDirectory.absolutePath
                                )
                            )
                            .toDesignJson()
                    )
                }
            )
            put(
                "buildLogic",
                buildJsonObject {
                    val source = ProjectCatalogTreeSource.BuildLogicSettings(
                        settingsFilePath = request.buildLogicSettingsFile.absolutePath
                    )
                    put(
                        "libraries",
                        projectCatalogTreesPort
                            .readLibraryTree(source)
                            .toDesignJson()
                    )
                    put(
                        "plugins",
                        projectCatalogTreesPort
                            .readPluginTree(source)
                            .toDesignJson()
                    )
                }
            )
        }

    private fun buildModuleDependencies(request: FigmaDesignModelGenerationRequest) =
        buildJsonObject {
            put(
                "main",
                projectModuleDependenciesPort
                    .readModuleDependencies(
                        ProjectModuleDependenciesSource(
                            rootDirPath = request.projectRootDirectory.absolutePath,
                            scope = ProjectModuleDependenciesScope.Main
                        )
                    )
                    .toModuleDependenciesJson()
            )
            put(
                "buildLogic",
                projectModuleDependenciesPort
                    .readModuleDependencies(
                        ProjectModuleDependenciesSource(
                            rootDirPath = request.buildLogicRootDirectory.absolutePath,
                            scope = ProjectModuleDependenciesScope.BuildLogic
                        )
                    )
                    .toModuleDependenciesJson()
            )
        }

    private companion object {
        const val SCHEMA_VERSION = 1
    }
}
