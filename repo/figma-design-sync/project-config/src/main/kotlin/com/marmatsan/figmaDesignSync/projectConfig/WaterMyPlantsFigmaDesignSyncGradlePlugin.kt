package com.marmatsan.figmaDesignSync.projectConfig

import com.marmatsan.figmaDesignSync.plugin.gradle.figmaDesignSyncExtension
import com.marmatsan.figmaDesignSync.teamcityAdapter.TeamCityCiConfigurationProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import java.io.File

/**
 * Water My Plants adapter for the portable Figma design-sync Gradle plugin.
 *
 * This is the only Kotlin production type allowed to know the repository
 * layout, Figma document identity, primary catalog name, or TeamCity command.
 */
class WaterMyPlantsFigmaDesignSyncGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("com.marmatsan.figmaDesignSync")

        project.extensions.configure<figmaDesignSyncExtension> {
            designModelMetadataNodeUrl.set(
                "https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=62934-908"
            )
            metadataNamespace.set("water_my_plants_sync")
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
                    "repo/figma-design-sync/project-config/water-my-plants/change-impact-policy.json"
                )
            )
            toolsDirectory.set(project.layout.projectDirectory.dir("repo/figma-design-sync/tools"))
            ciConfigurationCommand.set(teamCityConfigurationCommand(project))

            includedBuilds.register("dependency-catalog") {
                modelName.set("dependencyCatalog")
                settingsFile.set(project.layout.projectDirectory.file("repo/dependency-catalog/settings.gradle.kts"))
                rootDirectory.set(project.layout.projectDirectory.dir("repo/dependency-catalog"))
                modulePathPrefix.set(":dependency-catalog")
                publishesCatalogs.set(false)
            }

            includedBuilds.register("figma-design-sync") {
                modelName.set("figmaDesignSync")
                settingsFile.set(project.layout.projectDirectory.file("repo/figma-design-sync/settings.gradle.kts"))
                rootDirectory.set(project.layout.projectDirectory.dir("repo/figma-design-sync"))
                modulePathPrefix.set(":figma-design-sync")
            }

            includedBuilds.register("gradle-plugins") {
                modelName.set("gradlePlugins")
                settingsFile.set(project.layout.projectDirectory.file("repo/gradle-plugins/settings.gradle.kts"))
                rootDirectory.set(project.layout.projectDirectory.dir("repo/gradle-plugins"))
                modulePathPrefix.set(":gradle-plugins")
                publishesConventionPlugins.set(true)
            }
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
            projectRootDirectory.set(project.layout.projectDirectory)
            toolsDirectory.set(project.layout.projectDirectory.dir("repo/figma-design-sync/tools"))
            skipExecutorBuild.convention(
                project.providers.gradleProperty("figmaSkipExecutorBuild").map(String::toBoolean).orElse(false)
            )
            expectedGitSha.convention(project.providers.gradleProperty("figmaExpectedGitSha"))
            mainBranchAliases.set(listOf("main", "<default>", "refs/heads/main"))
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

    private fun teamCityConfigurationCommand(project: Project): List<String> {
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

        return if (isWindows()) listOf("cmd.exe", "/d", "/c") + arguments else arguments
    }

    private fun isWindows(): Boolean =
        System.getProperty("os.name").startsWith("Windows", ignoreCase = true)
}
