package com.marmatsan.figmaDocumentationSync.projectConfig

import com.marmatsan.figmaDocumentationSync.data.json.writer.FigmaWriterProjectConfigJson
import com.marmatsan.figmaDocumentationSync.plugin.gradle.figmaDocumentationSyncExtension
import com.marmatsan.figmaDocumentationSync.plugin.task.config.WriteFigmaWriterProjectConfigTask
import com.marmatsan.figmaDocumentationSync.plugin.task.mcp.ProbeFigmaMcpTask
import com.marmatsan.figmaDocumentationSync.plugin.task.mcp.RunFigmaMcpTask
import com.marmatsan.figmaDocumentationSync.plugin.task.official.PrepareOfficialFigmaSyncTask
import com.marmatsan.figmaDocumentationSync.plugin.task.visual.GenerateCiVisualPlanTask
import com.marmatsan.figmaDocumentationSync.teamcityAdapter.TeamCityCiConfigurationProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import java.io.File

/**
 * Water My Plants adapter for the portable Figma design-sync Gradle plugin.
 *
 * This is the only Kotlin production type allowed to know the repository
 * layout, Figma document identity, primary catalog name, or TeamCity command.
 */
class WaterMyPlantsFigmaDocumentationSyncGradlePlugin : Plugin<Project> {
    override fun apply(
        project: Project
    ) {
        project.pluginManager.apply("com.marmatsan.figmaDocumentationSync")
        val writerConfig = WaterMyPlantsFigmaWriterProjectConfig.value

        project.extensions.configure<figmaDocumentationSyncExtension> {
            designModelMetadataNodeUrl.set(
                "https://www.figma.com/design/${writerConfig.figmaFileKey}/Water-My-Plants" +
                    "?node-id=${writerConfig.metadataPageId.replace(
                        ':',
                        '-'
                    )}"
            )
            metadataNamespace.set(writerConfig.metadataNamespace)
            primaryCatalogModelName.set("waterMyPlants")
            dependencyCatalogProviderClassName.set(
                WaterMyPlantsDependencyCatalogProvider::class.java.name
            )
            ciDocumentationEnabled.set(true)
            ciConfigurationModelName.set("teamCity")
            ciConfigurationProviderClassName.set(TeamCityCiConfigurationProvider::class.java.name)
            ciDefaultBranchAlias.set("<default>")
            versionsFile.set(project.layout.projectDirectory.file("repo/dependency-catalog/versions.properties"))
            ciExternalTopologyFile.set(project.layout.projectDirectory.file("docs/ci/external-topology.yaml"))
            ciWindowsRuntimeFile.set(project.layout.projectDirectory.file("docs/ci/windows-runtime.yaml"))
            ciGeneratedConfigurationDirectory.set(
                project.layout.projectDirectory.dir(".teamcity/target/generated-configs")
            )
            changeImpactPolicyFile.set(
                project.layout.projectDirectory.file(
                    "repo/figma-documentation-sync/project-config/water-my-plants/change-impact-policy.json"
                )
            )
            toolsDirectory.set(project.layout.projectDirectory.dir("repo/figma-documentation-sync/tools"))
            ciConfigurationCommand.set(
                teamCityConfigurationCommand(
                    project = project
                )
            )

            includedBuilds.register("dependency-catalog") {
                modelName.set("dependencyCatalog")
                settingsFile.set(project.layout.projectDirectory.file("repo/dependency-catalog/settings.gradle.kts"))
                rootDirectory.set(project.layout.projectDirectory.dir("repo/dependency-catalog"))
                modulePathPrefix.set(":dependency-catalog")
                publishesCatalogs.set(false)
            }

            includedBuilds.register("figma-documentation-sync") {
                modelName.set("figmaDocumentationSync")
                settingsFile.set(project.layout.projectDirectory.file("repo/figma-documentation-sync/settings.gradle.kts"))
                rootDirectory.set(project.layout.projectDirectory.dir("repo/figma-documentation-sync"))
                modulePathPrefix.set(":figma-documentation-sync")
            }

            includedBuilds.register("gradle-plugins") {
                modelName.set("gradlePlugins")
                settingsFile.set(project.layout.projectDirectory.file("repo/gradle-plugins/settings.gradle.kts"))
                rootDirectory.set(project.layout.projectDirectory.dir("repo/gradle-plugins"))
                modulePathPrefix.set(":gradle-plugins")
                publishesConventionPlugins.set(true)
            }
        }

        val writeWriterProjectConfig =
            project.tasks.register<WriteFigmaWriterProjectConfigTask>("writeFigmaWriterProjectConfig") {
                group = "figma design sync"
                description = "Writes the Water My Plants Figma writer configuration as transient JSON."
                configurationJson.set(FigmaWriterProjectConfigJson.encode(writerConfig))
                outputFile.set(
                    project.layout.buildDirectory.file(
                        "generated/figma-documentation-sync/writer-project-config.json"
                    )
                )
            }

        val generatedWriterProjectConfigFile = writeWriterProjectConfig.flatMap { task -> task.outputFile }
        val toolsDirectory = project.layout.projectDirectory.dir("repo/figma-documentation-sync/tools")

        project.tasks.register<Exec>("buildFigmaDocumentationSyncTools") {
            group = "figma design sync"
            description = "Builds the TypeScript Figma boundary from the Kotlin project configuration."
            dependsOn(writeWriterProjectConfig)
            inputs.file(generatedWriterProjectConfigFile)
            workingDir(toolsDirectory)
            doFirst {
                commandLine(
                    "node",
                    "bin/build.mjs",
                    "--project-config-json=${generatedWriterProjectConfigFile.get().asFile.absolutePath}",
                    "--output-dir=."
                )
            }
        }

        project.tasks.register<Exec>("testFigmaDocumentationSyncTools") {
            group = "verification"
            description = "Tests the TypeScript Figma boundary against the Kotlin project configuration."
            dependsOn(writeWriterProjectConfig)
            inputs.file(generatedWriterProjectConfigFile)
            workingDir(toolsDirectory)
            commandLine(
                npmExecutable(),
                "test"
            )
            doFirst {
                environment(
                    "FIGMA_DOCUMENTATION_SYNC_PROJECT_CONFIG",
                    generatedWriterProjectConfigFile.get().asFile.absolutePath
                )
            }
        }

        project.tasks.named<PrepareOfficialFigmaSyncTask>("prepareOfficialFigmaSync") {
            dependsOn(writeWriterProjectConfig)
            writerProjectConfigFile.set(generatedWriterProjectConfigFile)
        }

        project.tasks.named<GenerateCiVisualPlanTask>("generateFigmaCiVisualPlan") {
            dependsOn(writeWriterProjectConfig)
            writerProjectConfigFile.set(writeWriterProjectConfig.flatMap { task -> task.outputFile })
        }

        project.tasks.named<RunFigmaMcpTask>("runFigmaMcp") {
            dependsOn(writeWriterProjectConfig)
            writerProjectConfigFile.set(writeWriterProjectConfig.flatMap { task -> task.outputFile })
        }

        project.tasks.named<ProbeFigmaMcpTask>("probeFigmaMcp") {
            dependsOn(writeWriterProjectConfig)
            writerProjectConfigFile.set(writeWriterProjectConfig.flatMap { task -> task.outputFile })
        }

        project.tasks.register<PrepareTeamCityFigmaSyncHandoffTask>("prepareTeamCityFigmaSyncHandoff") {
            group = "documentation"
            description = "Downloads or opens official TeamCity artifacts and prepares the Figma MCP handoff."
            buildId.convention(
                project.providers.gradleProperty("figmaTeamCityBuildId").map(String::toLong)
            )
            artifactDirectory.set(
                project.layout.dir(
                    project.providers.gradleProperty("figmaArtifactDirectory").map(::File)
                )
            )
            destinationRoot.convention(
                project.layout.dir(
                    project.providers.gradleProperty("figmaHandoffDestinationRoot").map(::File)
                ).orElse(project.layout.projectDirectory.dir("tmp/teamcity"))
            )
            expectedGitSha.convention(project.providers.gradleProperty("figmaExpectedGitSha"))
            mainBranchAliases.set(
                listOf(
                    "main",
                    "<default>",
                    "refs/heads/main"
                )
            )
            requiredBuildTypeName.set("Generate main design model")
        }

        project.tasks.register<UploadOfficialFigmaPayloadTask>("uploadOfficialFigmaPayload") {
            group = "documentation"
            description = "Uploads the verified PNG from one successful main TeamCity Figma artifact set."
            buildId.convention(
                project.providers.gradleProperty("figmaTeamCityBuildId").map(String::toLong)
            )
            artifactDirectory.set(
                project.layout.dir(
                    project.providers.gradleProperty("figmaArtifactDirectory").map(::File)
                )
            )
            uploadUrl.convention(project.providers.gradleProperty("figmaMcpUploadUrl"))
            destinationRoot.convention(
                project.layout.dir(
                    project.providers.gradleProperty("figmaHandoffDestinationRoot").map(::File)
                ).orElse(project.layout.projectDirectory.dir("tmp/teamcity"))
            )
            projectDirectory.set(project.layout.projectDirectory)
            expectedGitSha.convention(project.providers.gradleProperty("figmaExpectedGitSha"))
            mainBranchAliases.set(
                listOf(
                    "main",
                    "<default>",
                    "refs/heads/main"
                )
            )
            requiredBuildTypeName.set("Generate main design model")
        }

        project.tasks.register<RerunTeamCityFigmaSyncTask>("rerunTeamCityFigmaSync") {
            group = "documentation"
            description = "Validates or reruns the official TeamCity Figma Sync pipeline."
            serverUrl.convention(
                project.providers.gradleProperty("figmaTeamCityServerUrl")
                    .orElse("https://teamcity.marmatsan.dev")
            )
            validateOnly.convention(
                project.providers.gradleProperty("figmaTeamCityValidateOnly")
                    .map(String::toBoolean)
                    .orElse(false)
            )
            waitForCompletion.convention(
                project.providers.gradleProperty("figmaTeamCityWait")
                    .map(String::toBoolean)
                    .orElse(false)
            )
            pollIntervalSeconds.convention(
                project.providers.gradleProperty("figmaTeamCityPollIntervalSeconds")
                    .map(String::toInt)
                    .orElse(10)
            )
            timeoutMinutes.convention(
                project.providers.gradleProperty("figmaTeamCityTimeoutMinutes")
                    .map(String::toInt)
                    .orElse(60)
            )
            buildTypeId.set("WaterMyPlants_WaterMyPlantsFigmaSync")
            branch.set("main")
        }
    }

    private fun teamCityConfigurationCommand(
        project: Project
    ): List<String> {
        val wrapper = project.layout.projectDirectory
            .file(if (isWindows()) "mvnw.cmd" else "mvnw")
            .asFile
            .absolutePath
        val arguments = listOf(
            wrapper,
            "-f",
            project.layout.projectDirectory.file(".teamcity/pom.xml").asFile.absolutePath,
            "teamcity-configs:generate"
        )

        return if (isWindows()) listOf(
            "cmd.exe",
            "/d",
            "/c"
        ) + arguments else arguments
    }

    private fun isWindows(): Boolean =
        System.getProperty("os.name").startsWith(
            "Windows",
            ignoreCase = true
        )

    private fun npmExecutable(): String = if (isWindows()) "npm.cmd" else "npm"
}
