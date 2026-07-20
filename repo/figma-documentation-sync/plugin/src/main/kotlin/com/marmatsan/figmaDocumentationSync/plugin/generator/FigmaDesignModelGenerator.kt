package com.marmatsan.figmaDocumentationSync.plugin.generator

import com.marmatsan.figmaDocumentationSync.domain.port.catalog.ProjectCatalogTreeSource
import com.marmatsan.figmaDocumentationSync.domain.port.catalog.ProjectCatalogTreesPort
import com.marmatsan.figmaDocumentationSync.domain.port.ci.CiExternalTopologyPort
import com.marmatsan.figmaDocumentationSync.domain.port.ci.CiExternalTopologySource
import com.marmatsan.figmaDocumentationSync.domain.port.ci.CiWindowsRuntimePort
import com.marmatsan.figmaDocumentationSync.domain.port.ci.CiWindowsRuntimeSource
import com.marmatsan.figmaDocumentationSync.domain.port.ci.CiConfigurationPort
import com.marmatsan.figmaDocumentationSync.domain.port.ci.CiGeneratedConfigurationSource
import com.marmatsan.figmaDocumentationSync.domain.port.gradle.IncludedBuildSource
import com.marmatsan.figmaDocumentationSync.domain.port.modules.ProjectModuleDependenciesPort
import com.marmatsan.figmaDocumentationSync.domain.port.modules.ProjectModuleDependenciesScope
import com.marmatsan.figmaDocumentationSync.domain.port.modules.ProjectModuleDependenciesSource
import com.marmatsan.figmaDocumentationSync.domain.port.modules.ProjectModulesPort
import com.marmatsan.figmaDocumentationSync.domain.port.modules.ProjectModulesSource
import com.marmatsan.figmaDocumentationSync.domain.port.versions.RepositoryVersionsPort
import com.marmatsan.figmaDocumentationSync.domain.port.versions.VersionsFileSource
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
    private val projectModuleDependenciesPort: ProjectModuleDependenciesPort,
    private val ciExternalTopologyPort: CiExternalTopologyPort,
    private val ciWindowsRuntimePort: CiWindowsRuntimePort,
    private val ciConfigurationPort: CiConfigurationPort
) {
    /**
     * Generates the complete model and stable model hash for [request].
     *
     * The hash input contains schema version and content. Git identity fields
     * are written to the model as traceability metadata, but they are excluded
     * from the hash so commits that do not affect the visual model do not force
     * a Figma sync.
     */
    fun generate(
        request: FigmaDesignModelGenerationRequest
    ): FigmaDesignModelGenerationResult {
        val content = buildContent(
            request = request
        )
        val hashInput = buildJsonObject {
            put(
                "schemaVersion",
                SCHEMA_VERSION
            )
            put(
                "content",
                content
            )
        }
        val modelHash = FigmaDesignModelHash.compute(hashInput)
        val model = buildJsonObject {
            put(
                "schemaVersion",
                SCHEMA_VERSION
            )
            put(
                "branch",
                request.branch
            )
            put(
                "gitSha",
                request.gitSha
            )
            put(
                "generatedAt",
                request.generatedAt.toString()
            )
            put(
                "content",
                content
            )
            put(
                "modelHash",
                modelHash
            )
        }

        return FigmaDesignModelGenerationResult(
            model = model,
            modelHash = modelHash
        )
    }

    private fun buildContent(
        request: FigmaDesignModelGenerationRequest
    ) =
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
            put(
                "versionSections",
                versionSections.toVersionSectionsJson()
            )
            put(
                "catalogs",
                buildCatalogs(
                    request = request
                )
            )
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
            put(
                "moduleDependencies",
                buildModuleDependencies(
                    request = request
                )
            )
            if (request.ciDocumentationEnabled) {
                put(
                    "ci",
                    buildCi(
                        request = request
                    )
                )
            }
        }

    private fun buildCi(
        request: FigmaDesignModelGenerationRequest
    ) =
        buildJsonObject {
            put(
                "externalTopology",
                ciExternalTopologyPort
                    .readTopology(
                        CiExternalTopologySource(
                            request.ciExternalTopologyFile.requireCiInput("external topology").absolutePath
                        )
                    )
                    .toDesignJson()
            )
            put(
                "windowsRuntime",
                ciWindowsRuntimePort
                    .readRuntime(
                        CiWindowsRuntimeSource(
                            request.ciWindowsRuntimeFile.requireCiInput("Windows runtime").absolutePath
                        )
                    )
                    .toDesignJson()
            )
            put(
                request.ciConfigurationModelName.requireCiInput("configuration model name"),
                ciConfigurationPort
                    .readConfiguration(
                        CiGeneratedConfigurationSource(
                            directoryPath = request.ciGeneratedConfigurationDirectory
                                .requireCiInput("generated configuration")
                                .absolutePath,
                            providerClassName = request.ciConfigurationProviderClassName
                                .requireCiInput("configuration provider class name")
                        )
                    )
                    .toDesignJson()
            )
        }

    private fun buildCatalogs(
        request: FigmaDesignModelGenerationRequest
    ) =
        buildJsonObject {
            val conventionPluginIncludedBuilds = request.includedBuilds
                .map(FigmaDesignModelIncludedBuildSource::toDomainSource)
                .filter(IncludedBuildSource::publishesConventionPlugins)
            put(
                request.primaryCatalogModelName,
                buildJsonObject {
                    put(
                        "libraries",
                        projectCatalogTreesPort
                            .readLibraryTree(
                                ProjectCatalogTreeSource.DependenciesDslVersionAliases(
                                    rootDirPath = request.projectRootDirectory.absolutePath,
                                    providerClassName = request.dependencyCatalogProviderClassName,
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
                                    providerClassName = request.dependencyCatalogProviderClassName,
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
            request.includedBuilds
                .filter(FigmaDesignModelIncludedBuildSource::publishesCatalogs)
                .forEach { includedBuild ->
                    put(
                        includedBuild.modelName,
                        buildJsonObject {
                            val source = ProjectCatalogTreeSource.IncludedBuildSettings(
                                includedBuild = includedBuild.toDomainSource()
                            )
                            val libraries = projectCatalogTreesPort.readLibraryTree(source)
                            val plugins = projectCatalogTreesPort.readPluginTree(source)

                            if (libraries.roots.isNotEmpty()) {
                                put(
                                    "libraries",
                                    libraries.toDesignJson()
                                )
                            }
                            if (plugins.roots.isNotEmpty()) {
                                put(
                                    "plugins",
                                    plugins.toDesignJson()
                                )
                            }
                        }
                    )
                }
        }

    private fun buildModuleDependencies(
        request: FigmaDesignModelGenerationRequest
    ) =
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
        const val SCHEMA_VERSION = 4
    }
}

private fun java.io.File?.requireCiInput(
    name: String
): java.io.File =
    requireNotNull(this) {
        "CI documentation is enabled, but its $name input is not configured."
    }

private fun String?.requireCiInput(
    name: String
): String =
    requireNotNull(this?.takeIf(String::isNotBlank)) {
        "CI documentation is enabled, but its $name input is not configured."
    }
