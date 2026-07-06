package com.marmatsan.figmaDesignSync.plugin.generator

import com.marmatsan.figmaDesignSync.domain.port.catalog.ProjectCatalogTreeSource
import com.marmatsan.figmaDesignSync.domain.port.catalog.ProjectCatalogTreesPort
import com.marmatsan.figmaDesignSync.domain.port.gradle.IncludedBuildSource
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

/**
 * Builds the executable `design-model.json` contract consumed by Figma sync.
 *
 * The generator is the orchestration point between domain ports and the JSON
 * artifact. It asks ports for versions, catalogs, modules, and module
 * dependency edges, then serializes those values into a deterministic model
 * shape.
 *
 * @see FigmaDesignModelGenerationRequest
 * @see FigmaDesignModelGenerationResult
 * @see FigmaDesignModelHash
 */
@Inject
internal class FigmaDesignModelGenerator(
    private val repositoryVersionsPort: RepositoryVersionsPort,
    private val projectCatalogTreesPort: ProjectCatalogTreesPort,
    private val projectModulesPort: ProjectModulesPort,
    private val projectModuleDependenciesPort: ProjectModuleDependenciesPort
) {
    /**
     * Generates the complete model and stable model hash for [request].
     *
     * The hash input contains schema version and content. Git identity fields
     * are written to the model as traceability metadata, but they are excluded
     * from the hash so commits that do not affect the visual model do not force
     * a Figma sync.
     */
    fun generate(request: FigmaDesignModelGenerationRequest): FigmaDesignModelGenerationResult {
        val content = buildContent(request)
        val hashInput = buildJsonObject {
            put("schemaVersion", SCHEMA_VERSION)
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
            val includedBuilds = request.includedBuilds.map(FigmaDesignModelIncludedBuildSource::toDomainSource)
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
                            includedBuilds = includedBuilds
                        )
                    )
                    .toSortedJsonArray()
            )
            put("moduleDependencies", buildModuleDependencies(request))
        }

    private fun buildCatalogs(request: FigmaDesignModelGenerationRequest) =
        buildJsonObject {
            val conventionPluginIncludedBuilds = request.includedBuilds
                .map(FigmaDesignModelIncludedBuildSource::toDomainSource)
                .filter(IncludedBuildSource::publishesConventionPlugins)
            put(
                "waterMyPlants",
                buildJsonObject {
                    put(
                        "libraries",
                        projectCatalogTreesPort
                            .readLibraryTree(
                                ProjectCatalogTreeSource.DependenciesDslVersionAliases(
                                    rootDirPath = request.projectRootDirectory.absolutePath,
                                    conventionPluginIncludedBuilds = conventionPluginIncludedBuilds
                                )
                            )
                            .toDesignJson()
                    )
                    put(
                        "plugins",
                        projectCatalogTreesPort
                            .readPluginTree(
                                ProjectCatalogTreeSource.DependenciesDslVersionAliases(
                                    rootDirPath = request.projectRootDirectory.absolutePath,
                                    conventionPluginIncludedBuilds = conventionPluginIncludedBuilds
                                )
                            )
                            .toDesignJson()
                    )
                    put(
                        "customGradleConventionPlugins",
                        projectCatalogTreesPort
                            .readPluginTree(
                                ProjectCatalogTreeSource.CustomGradleConventionPlugins(
                                    rootDirPath = request.projectRootDirectory.absolutePath,
                                    includedBuilds = conventionPluginIncludedBuilds
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
            request.includedBuilds.forEach { includedBuild ->
                put(
                    includedBuild.modelName,
                    buildJsonObject {
                        val source = ProjectCatalogTreeSource.IncludedBuildSettings(
                            includedBuild = includedBuild.toDomainSource()
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
            request.includedBuilds
                .forEach { includedBuild ->
                    put(
                        includedBuild.modelName,
                        projectModuleDependenciesPort
                            .readModuleDependencies(
                                ProjectModuleDependenciesSource(
                                    rootDirPath = includedBuild.rootDirectory.absolutePath,
                                    scope = ProjectModuleDependenciesScope.IncludedBuild,
                                    modulePathPrefix = includedBuild.modulePathPrefix
                                )
                            )
                            .toModuleDependenciesJson()
                    )
                }
        }

    private companion object {
        const val SCHEMA_VERSION = 1
    }
}
