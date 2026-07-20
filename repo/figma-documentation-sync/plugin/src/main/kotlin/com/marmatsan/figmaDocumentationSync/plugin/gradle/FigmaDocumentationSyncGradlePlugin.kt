package com.marmatsan.figmaDocumentationSync.plugin.gradle

import com.marmatsan.figmaDocumentationSync.domain.model.impact.FigmaVerificationScope
import com.marmatsan.figmaDocumentationSync.plugin.di.create
import com.marmatsan.figmaDocumentationSync.plugin.di.figmaDocumentationSyncComponent
import com.marmatsan.figmaDocumentationSync.plugin.generator.FigmaDesignModelIncludedBuildSource
import com.marmatsan.figmaDocumentationSync.plugin.task.artifact.ValidateOfficialFigmaArtifactSetTask
import com.marmatsan.figmaDocumentationSync.plugin.task.catalog.CheckFigmaCatalogUsageTask
import com.marmatsan.figmaDocumentationSync.plugin.task.ci.CheckCiExternalTopologyFreshnessTask
import com.marmatsan.figmaDocumentationSync.plugin.task.ci.CheckCiWindowsRuntimeFreshnessTask
import com.marmatsan.figmaDocumentationSync.plugin.task.generate.GenerateFigmaDesignModelTask
import com.marmatsan.figmaDocumentationSync.plugin.task.impact.ClassifyFigmaChangeImpactTask
import com.marmatsan.figmaDocumentationSync.plugin.task.mcp.ProbeFigmaMcpTask
import com.marmatsan.figmaDocumentationSync.plugin.task.mcp.RunFigmaMcpTask
import com.marmatsan.figmaDocumentationSync.plugin.task.official.PrepareOfficialFigmaSyncTask
import com.marmatsan.figmaDocumentationSync.plugin.task.official.ValidateOfficialFigmaSyncScopeTask
import com.marmatsan.figmaDocumentationSync.plugin.task.sync.CheckFigmaTrunkSyncTask
import com.marmatsan.figmaDocumentationSync.plugin.task.versions.CheckFigmaVersionNamingTask
import com.marmatsan.figmaDocumentationSync.plugin.task.visual.GenerateCiVisualPlanTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.language.base.plugins.LifecycleBasePlugin
import java.io.File

/**
 * Registers Gradle tasks that generate and verify the Figma design model.
 *
 * Apply plugin id `com.marmatsan.figmaDocumentationSync` on the repository root. The
 * plugin exposes the `figmaDocumentationSync` extension and creates:
 *
 * - `generateFigmaDesignModel`
 * - `generateFigmaCiVisualPlan`
 * - `prepareOfficialFigmaSync`
 * - `verifyOfficialFigmaSync`
 * - `checkFigmaCatalogUsage`
 * - `checkFigmaVersionNaming`
 * - `checkFigmaTrunkSync`
 */
@Suppress("unused")
class FigmaDocumentationSyncGradlePlugin : Plugin<Project> {
    override fun apply(
        project: Project,
    ) {
        project.pluginManager.apply("base")

        val extension = project.extensions.create<figmaDocumentationSyncExtension>("figmaDocumentationSync")

        val includedBuildSources =
            extension.includedBuildSources(
                project = project,
            )

        project.tasks.register<GenerateCiVisualPlanTask>("generateFigmaCiVisualPlan") {
            group = "documentation"
            description = "Generates the Kotlin-owned CI visual plan consumed by the Figma adapter."

            designModelFile.set(
                project.layout
                    .file(
                        project.providers.gradleProperty("figmaCiVisualDesignModel").map(
                            ::File,
                        ),
                    ).orElse(extension.designModelFile),
            )
            writerProjectConfigFile.set(
                project.layout.file(
                    project.providers.gradleProperty("figmaWriterProjectConfig").map(
                        ::File,
                    ),
                ),
            )
            target.convention(project.providers.gradleProperty("figmaCiVisualTarget"))
            outputFile.set(
                project.layout
                    .file(
                        project.providers.gradleProperty("figmaCiVisualPlanOutput").map(
                            ::File,
                        ),
                    ).orElse(project.layout.buildDirectory.file("reports/figma-sync/ci-visual-plan.json")),
            )
        }

        project.tasks.register<ClassifyFigmaChangeImpactTask>("classifyFigmaChangeImpact") {
            group = "verification"
            description = "Classifies the current repository change for Figma verification and sync."

            policyFile.set(extension.changeImpactPolicyFile)
            projectRootDirectory.set(project.layout.projectDirectory)
            outputFile.set(extension.changeImpactFile)
            changedPathsOverride.convention(
                project.providers
                    .gradleProperty("figmaChangedPaths")
                    .map { value ->
                        value
                            .split(',')
                            .map(
                                transform = String::trim,
                            ).filter(String::isNotEmpty)
                    }.orElse(emptyList()),
            )
            project.providers
                .gradleProperty("figmaComparisonBase")
                .orNull
                ?.let(comparisonBaseOverride::set)
            outputs.upToDateWhen { false }
        }

        project.tasks.register<ValidateOfficialFigmaArtifactSetTask>("validateOfficialFigmaArtifactSet") {
            group = "verification"
            description = "Validates an official main Figma artifact set and writes its handoff identity."

            artifactDirectory.set(
                project.layout.dir(
                    project.providers.gradleProperty("figmaArtifactDirectory").map(
                        ::File,
                    ),
                ),
            )
            expectedGitSha.convention(project.providers.gradleProperty("figmaExpectedGitSha"))
            outputFile.set(
                project.layout
                    .file(
                        project.providers.gradleProperty("figmaArtifactValidationOutput").map(
                            ::File,
                        ),
                    ).orElse(project.layout.buildDirectory.file("reports/figma-sync/validated-artifact-set.json")),
            )
        }

        project.tasks.register<RunFigmaMcpTask>("runFigmaMcp") {
            group = "documentation"
            description = "Inspects, records, or executes a generated runner through the Kotlin MCP client."

            manifestPath.convention(project.providers.gradleProperty("figmaMcpManifest"))
            planPath.convention(project.providers.gradleProperty("figmaMcpPlan"))
            statePath.convention(project.providers.gradleProperty("figmaMcpState"))
            visualStatePath.convention(project.providers.gradleProperty("figmaMcpVisualState"))
            endpoint.convention(
                project.providers.gradleProperty("figmaMcpEndpoint").orElse("http://127.0.0.1:3845/mcp"),
            )
            resume.convention(
                booleanProperty(
                    project = project,
                    name = "figmaMcpResume",
                ),
            )
            retryFailed.convention(
                booleanProperty(
                    project = project,
                    name = "figmaMcpRetryFailed",
                ),
            )
            reuseStaging.convention(
                booleanProperty(
                    project = project,
                    name = "figmaMcpReuseStaging",
                ),
            )
            dryRun.convention(
                booleanProperty(
                    project = project,
                    name = "figmaMcpDryRun",
                ),
            )
            next.convention(
                booleanProperty(
                    project = project,
                    name = "figmaMcpNext",
                ),
            )
            from.convention(project.providers.gradleProperty("figmaMcpFrom"))
            recordSuccess.convention(project.providers.gradleProperty("figmaMcpRecordSuccess"))
            recordFailure.convention(project.providers.gradleProperty("figmaMcpRecordFailure"))
            summary.convention(project.providers.gradleProperty("figmaMcpSummary"))
            writerProjectConfigFile.set(
                project.layout.file(
                    project.providers.gradleProperty("figmaWriterProjectConfig").map(
                        ::File,
                    ),
                ),
            )
        }

        project.tasks.register<ProbeFigmaMcpTask>("probeFigmaMcp") {
            group = "verification"
            description = "Probes the local MCP endpoint with the official Kotlin SDK client."

            endpoint.convention(
                project.providers.gradleProperty("figmaMcpEndpoint").orElse("http://127.0.0.1:3845/mcp"),
            )
            writerProjectConfigFile.set(
                project.layout.file(
                    project.providers.gradleProperty("figmaWriterProjectConfig").map(
                        ::File,
                    ),
                ),
            )
        }

        val checkFigmaCatalogUsage =
            project.tasks.register<CheckFigmaCatalogUsageTask>("checkFigmaCatalogUsage") {
                group = "verification"
                description = "Checks that dependency catalog entries rendered in Figma are used by the repository."

                primaryCatalogModelName.set(extension.primaryCatalogModelName)
                dependencyCatalogProviderClassName.set(extension.dependencyCatalogProviderClassName)
                rootSettingsFile.set(extension.rootSettingsFile)
                includedBuildSettingsFiles.from(
                    includedBuildSources.map { sources -> sources.map { source -> source.settingsFile } },
                )
                includedBuildModelNames.set(
                    includedBuildSources.map { sources -> sources.map { source -> source.modelName } },
                )
                includedBuildModulePathPrefixes.set(
                    includedBuildSources.map { sources -> sources.map { source -> source.modulePathPrefix } },
                )
                includedBuildPublishesCatalogs.set(
                    includedBuildSources.map { sources -> sources.map { source -> source.publishesCatalogs } },
                )
                includedBuildPublishesConventionPlugins.set(
                    includedBuildSources.map { sources -> sources.map { source -> source.publishesConventionPlugins } },
                )
                projectRootDirectory.set(project.layout.projectDirectory)
                includedBuildSourcesProvider = includedBuildSources
            }
        val checkFigmaVersionNaming =
            project.tasks.register<CheckFigmaVersionNamingTask>("checkFigmaVersionNaming") {
                group = "verification"
                description = "Checks that dependency version keys follow the Figma section naming contract."

                versionsFile.set(extension.versionsFile)
            }
        val checkCiExternalTopologyFreshness =
            project.tasks.register<CheckCiExternalTopologyFreshnessTask>("checkCiExternalTopologyFreshness") {
                group = "verification"
                description = "Warns when the external CI topology has not been validated recently."

                ciExternalTopologyFile.set(extension.ciExternalTopologyFile)
                onlyIf("CI documentation adapter is enabled") {
                    extension.ciDocumentationEnabled.get()
                }
            }
        val checkCiWindowsRuntimeFreshness =
            project.tasks.register<CheckCiWindowsRuntimeFreshnessTask>("checkCiWindowsRuntimeFreshness") {
                group = "verification"
                description = "Warns when the Windows CI runtime has not been validated recently."

                ciWindowsRuntimeFile.set(extension.ciWindowsRuntimeFile)
                onlyIf("CI documentation adapter is enabled") {
                    extension.ciDocumentationEnabled.get()
                }
            }

        project.tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME) {
            dependsOn(checkFigmaCatalogUsage)
            dependsOn(checkFigmaVersionNaming)
            dependsOn(checkCiExternalTopologyFreshness)
            dependsOn(checkCiWindowsRuntimeFreshness)
        }

        project.tasks.register<GenerateFigmaDesignModelTask>("generateFigmaDesignModel") {
            group = "documentation"
            description = "Generates the design model JSON consumed by the Figma MCP sync step."

            primaryCatalogModelName.set(extension.primaryCatalogModelName)
            dependencyCatalogProviderClassName.set(extension.dependencyCatalogProviderClassName)
            ciDocumentationEnabled.set(extension.ciDocumentationEnabled)
            ciConfigurationModelName.set(extension.ciConfigurationModelName)
            ciConfigurationProviderClassName.set(extension.ciConfigurationProviderClassName)
            ciDefaultBranchAlias.set(extension.ciDefaultBranchAlias)
            versionsFile.set(extension.versionsFile)
            rootSettingsFile.set(extension.rootSettingsFile)
            ciExternalTopologyFile.set(extension.ciExternalTopologyFile)
            ciWindowsRuntimeFile.set(extension.ciWindowsRuntimeFile)
            ciGeneratedConfigurationDirectory.set(extension.ciGeneratedConfigurationDirectory)
            includedBuildSettingsFiles.from(
                includedBuildSources.map { sources -> sources.map { source -> source.settingsFile } },
            )
            includedBuildModelNames.set(
                includedBuildSources.map { sources -> sources.map { source -> source.modelName } },
            )
            includedBuildModulePathPrefixes.set(
                includedBuildSources.map { sources -> sources.map { source -> source.modulePathPrefix } },
            )
            includedBuildPublishesCatalogs.set(
                includedBuildSources.map { sources -> sources.map { source -> source.publishesCatalogs } },
            )
            includedBuildPublishesConventionPlugins.set(
                includedBuildSources.map { sources -> sources.map { source -> source.publishesConventionPlugins } },
            )
            projectRootDirectory.set(project.layout.projectDirectory)
            includedBuildSourcesProvider = includedBuildSources
            outputFile.set(extension.designModelFile)
        }

        project.tasks.register<CheckFigmaTrunkSyncTask>("checkFigmaTrunkSync") {
            group = "verification"
            description =
                "Checks that Figma sync metadata matches the design model generated from the current checkout."

            metadataNodeUrl.set(extension.designModelMetadataNodeUrl)
            metadataNamespace.set(extension.metadataNamespace)
            primaryCatalogModelName.set(extension.primaryCatalogModelName)
            dependencyCatalogProviderClassName.set(extension.dependencyCatalogProviderClassName)
            ciDocumentationEnabled.set(extension.ciDocumentationEnabled)
            ciConfigurationModelName.set(extension.ciConfigurationModelName)
            ciConfigurationProviderClassName.set(extension.ciConfigurationProviderClassName)
            versionsFile.set(extension.versionsFile)
            rootSettingsFile.set(extension.rootSettingsFile)
            ciExternalTopologyFile.set(extension.ciExternalTopologyFile)
            ciWindowsRuntimeFile.set(extension.ciWindowsRuntimeFile)
            ciGeneratedConfigurationDirectory.set(extension.ciGeneratedConfigurationDirectory)
            includedBuildSettingsFiles.from(
                includedBuildSources.map { sources -> sources.map { source -> source.settingsFile } },
            )
            includedBuildModelNames.set(
                includedBuildSources.map { sources -> sources.map { source -> source.modelName } },
            )
            includedBuildModulePathPrefixes.set(
                includedBuildSources.map { sources -> sources.map { source -> source.modulePathPrefix } },
            )
            includedBuildPublishesCatalogs.set(
                includedBuildSources.map { sources -> sources.map { source -> source.publishesCatalogs } },
            )
            includedBuildPublishesConventionPlugins.set(
                includedBuildSources.map { sources -> sources.map { source -> source.publishesConventionPlugins } },
            )
            projectRootDirectory.set(project.layout.projectDirectory)
            includedBuildSourcesProvider = includedBuildSources
            figmaToken.set(project.providers.environmentVariable("FIGMA_FILE_CONTENT_ACCESS_TOKEN"))
        }

        val cleanOfficialFigmaSyncReports =
            project.tasks.register<Delete>("cleanOfficialFigmaSyncReports") {
                group = "build"
                description = "Removes stale official Figma Sync reports before preparing a new scope."
                delete(project.layout.buildDirectory.dir("reports/figma-sync"))
            }

        val teamCityPhasedOfficialExecution =
            booleanProperty(
                project = project,
                name = "figmaOfficialTeamCityPhasedExecution",
            )

        val classifyOfficialFigmaSyncChangeImpact =
            project.tasks.register<ClassifyFigmaChangeImpactTask>("classifyOfficialFigmaSyncChangeImpact") {
                group = "verification"
                description = "Classifies the main revision used by the official Figma Sync pipeline."
                dependsOn(cleanOfficialFigmaSyncReports)

                policyFile.set(extension.changeImpactPolicyFile)
                projectRootDirectory.set(project.layout.projectDirectory)
                outputFile.set(extension.changeImpactFile)
                changedPathsOverride.convention(
                    project.providers
                        .gradleProperty("figmaChangedPaths")
                        .map { value ->
                            value
                                .split(',')
                                .map(
                                    transform = String::trim,
                                ).filter(String::isNotEmpty)
                        }.orElse(emptyList()),
                )
                project.providers
                    .gradleProperty("figmaComparisonBase")
                    .orNull
                    ?.let(comparisonBaseOverride::set)
                outputs.upToDateWhen { false }
            }

        val materializeFigmaSyncCiConfiguration =
            project.tasks.register<Exec>("materializeFigmaSyncCiConfiguration") {
                group = "documentation"
                description = "Runs the optional CI adapter when the Figma model can change."
                if (teamCityPhasedOfficialExecution.get()) {
                    mustRunAfter(classifyOfficialFigmaSyncChangeImpact)
                } else {
                    dependsOn(classifyOfficialFigmaSyncChangeImpact)
                }
                onlyIf("CI documentation adapter is enabled and Figma impact requires full verification") {
                    extension.ciDocumentationEnabled.get() &&
                        extension.ciConfigurationCommand.get().isNotEmpty() &&
                        isFullVerification(
                            changeImpactFile = extension.changeImpactFile.get().asFile,
                        )
                }
                workingDir(extension.ciConfigurationWorkingDirectory)
                doFirst {
                    commandLine(extension.ciConfigurationCommand.get())
                }
                outputs.dir(extension.ciGeneratedConfigurationDirectory)
                outputs.upToDateWhen { false }
            }

        val generateOfficialFigmaSyncModel =
            project.tasks.register<GenerateFigmaDesignModelTask>("generateOfficialFigmaSyncModel") {
                group = "documentation"
                description = "Generates the model required by the prepared official Figma Sync scope."
                if (teamCityPhasedOfficialExecution.get()) {
                    mustRunAfter(materializeFigmaSyncCiConfiguration)
                } else {
                    dependsOn(materializeFigmaSyncCiConfiguration)
                }
                onlyIf("Figma change impact requires full verification") {
                    isFullVerification(
                        changeImpactFile = extension.changeImpactFile.get().asFile,
                    )
                }

                primaryCatalogModelName.set(extension.primaryCatalogModelName)
                dependencyCatalogProviderClassName.set(extension.dependencyCatalogProviderClassName)
                ciDocumentationEnabled.set(extension.ciDocumentationEnabled)
                ciConfigurationModelName.set(extension.ciConfigurationModelName)
                ciConfigurationProviderClassName.set(extension.ciConfigurationProviderClassName)
                ciDefaultBranchAlias.set(extension.ciDefaultBranchAlias)
                versionsFile.set(extension.versionsFile)
                rootSettingsFile.set(extension.rootSettingsFile)
                ciExternalTopologyFile.set(extension.ciExternalTopologyFile)
                ciWindowsRuntimeFile.set(extension.ciWindowsRuntimeFile)
                ciGeneratedConfigurationDirectory.set(extension.ciGeneratedConfigurationDirectory)
                includedBuildSettingsFiles.from(
                    includedBuildSources.map { sources -> sources.map { source -> source.settingsFile } },
                )
                includedBuildModelNames.set(
                    includedBuildSources.map { sources -> sources.map { source -> source.modelName } },
                )
                includedBuildModulePathPrefixes.set(
                    includedBuildSources.map { sources -> sources.map { source -> source.modulePathPrefix } },
                )
                includedBuildPublishesCatalogs.set(
                    includedBuildSources.map { sources -> sources.map { source -> source.publishesCatalogs } },
                )
                includedBuildPublishesConventionPlugins.set(
                    includedBuildSources.map { sources -> sources.map { source -> source.publishesConventionPlugins } },
                )
                projectRootDirectory.set(project.layout.projectDirectory)
                includedBuildSourcesProvider = includedBuildSources
                outputFile.set(extension.designModelFile)
            }

        project.tasks.register<PrepareOfficialFigmaSyncTask>("prepareOfficialFigmaSync") {
            group = "documentation"
            description = "Prepares the official model, MCP runners, visual plan, and shared sync scope."
            if (teamCityPhasedOfficialExecution.get()) {
                mustRunAfter(generateOfficialFigmaSyncModel)
            } else {
                dependsOn(generateOfficialFigmaSyncModel)
            }

            changeImpactFile.set(extension.changeImpactFile)
            changeImpactPolicyFile.set(extension.changeImpactPolicyFile)
            designModelFile.set(extension.designModelFile)
            metadataNodeUrl.set(extension.designModelMetadataNodeUrl)
            metadataNamespace.set(extension.metadataNamespace)
            projectRootDirectory.set(project.layout.projectDirectory)
            toolsDirectory.set(extension.toolsDirectory)
            runnerOutputDirectory.set(project.layout.buildDirectory.dir("reports/figma-sync/mcp-runners"))
            visualSyncPlanFile.set(project.layout.buildDirectory.file("reports/figma-sync/visual-sync-plan.json"))
            scopeFile.set(project.layout.buildDirectory.file("reports/figma-sync/sync-scope.json"))
            runnerTransport.convention(
                project.providers.gradleProperty("figmaMcpTransport").orElse("png"),
            )
            runnerChunkSize.convention(
                project.providers
                    .gradleProperty("figmaMcpChunkSize")
                    .map(
                        String::toInt,
                    ).orElse(12_000),
            )
            outputs.upToDateWhen { false }
        }

        val verifiedOfficialScopeFile =
            project.layout.buildDirectory.file("tmp/figma-sync/verified-scope.txt")
        val validateOfficialFigmaSyncScope =
            project.tasks.register<ValidateOfficialFigmaSyncScopeTask>("validateOfficialFigmaSyncScope") {
                group = "verification"
                description = "Validates the official scope artifact before the conditional Figma metadata check."

                scopeFile.set(project.layout.buildDirectory.file("reports/figma-sync/sync-scope.json"))
                designModelFile.set(extension.designModelFile)
                projectRootDirectory.set(project.layout.projectDirectory)
                verifiedScopeFile.set(verifiedOfficialScopeFile)
                outputs.upToDateWhen { false }
            }

        val checkOfficialFigmaTrunkSync =
            project.tasks.register<CheckFigmaTrunkSyncTask>("checkOfficialFigmaTrunkSync") {
                group = "verification"
                description = "Checks Figma metadata only when the validated official scope can change the model."
                if (teamCityPhasedOfficialExecution.get()) {
                    mustRunAfter(validateOfficialFigmaSyncScope)
                } else {
                    dependsOn(validateOfficialFigmaSyncScope)
                }
                onlyIf("Validated Figma scope requires full verification") {
                    verifiedOfficialScopeFile
                        .get()
                        .asFile
                        .readText()
                        .trim() == FigmaVerificationScope.FULL_VERIFICATION.wireValue
                }

                metadataNodeUrl.set(extension.designModelMetadataNodeUrl)
                metadataNamespace.set(extension.metadataNamespace)
                primaryCatalogModelName.set(extension.primaryCatalogModelName)
                dependencyCatalogProviderClassName.set(extension.dependencyCatalogProviderClassName)
                ciDocumentationEnabled.set(extension.ciDocumentationEnabled)
                ciConfigurationModelName.set(extension.ciConfigurationModelName)
                ciConfigurationProviderClassName.set(extension.ciConfigurationProviderClassName)
                versionsFile.set(extension.versionsFile)
                rootSettingsFile.set(extension.rootSettingsFile)
                ciExternalTopologyFile.set(extension.ciExternalTopologyFile)
                ciWindowsRuntimeFile.set(extension.ciWindowsRuntimeFile)
                ciGeneratedConfigurationDirectory.set(extension.ciGeneratedConfigurationDirectory)
                includedBuildSettingsFiles.from(
                    includedBuildSources.map { sources -> sources.map { source -> source.settingsFile } },
                )
                includedBuildModelNames.set(
                    includedBuildSources.map { sources -> sources.map { source -> source.modelName } },
                )
                includedBuildModulePathPrefixes.set(
                    includedBuildSources.map { sources -> sources.map { source -> source.modulePathPrefix } },
                )
                includedBuildPublishesCatalogs.set(
                    includedBuildSources.map { sources -> sources.map { source -> source.publishesCatalogs } },
                )
                includedBuildPublishesConventionPlugins.set(
                    includedBuildSources.map { sources -> sources.map { source -> source.publishesConventionPlugins } },
                )
                projectRootDirectory.set(project.layout.projectDirectory)
                includedBuildSourcesProvider = includedBuildSources
                figmaToken.set(project.providers.environmentVariable("FIGMA_FILE_CONTENT_ACCESS_TOKEN"))
            }

        project.tasks.register("verifyOfficialFigmaSync") {
            group = "verification"
            description = "Validates the shared official scope and conditionally checks Figma trunk metadata."
            dependsOn(checkOfficialFigmaTrunkSync)
        }
    }

    private fun isFullVerification(
        changeImpactFile: File,
    ): Boolean =
        figmaDocumentationSyncComponent::class
            .create()
            .officialFigmaSyncScopeJson
            .readChangeImpact(
                sourcePath = changeImpactFile.absolutePath,
            ).scope == FigmaVerificationScope.FULL_VERIFICATION

    private fun booleanProperty(
        project: Project,
        name: String,
    ) =
        project.providers
            .gradleProperty(name)
            .map(
                String::toBoolean,
            ).orElse(false)
}

private fun figmaDocumentationSyncExtension.includedBuildSources(
    project: Project,
) =
    project.provider {
        includedBuilds
            .toList()
            .sortedBy(FigmaDocumentationSyncIncludedBuild::getName)
            .map { includedBuild ->
                FigmaDesignModelIncludedBuildSource(
                    modelName = includedBuild.modelName.get(),
                    settingsFile = includedBuild.settingsFile.get().asFile,
                    rootDirectory = includedBuild.rootDirectory.get().asFile,
                    modulePathPrefix = includedBuild.modulePathPrefix.get(),
                    publishesCatalogs = includedBuild.publishesCatalogs.get(),
                    publishesConventionPlugins = includedBuild.publishesConventionPlugins.get(),
                )
            }
    }
