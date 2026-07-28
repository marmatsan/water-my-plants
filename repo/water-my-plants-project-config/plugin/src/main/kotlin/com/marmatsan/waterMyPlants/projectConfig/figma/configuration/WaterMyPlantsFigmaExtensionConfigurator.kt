package com.marmatsan.waterMyPlants.projectConfig.figma.configuration

import com.marmatsan.figmaDocumentationSync.domain.model.writer.FigmaWriterProjectConfig
import com.marmatsan.figmaDocumentationSync.plugin.gradle.figmaDocumentationSyncExtension
import com.marmatsan.figmaDocumentationSync.teamcityAdapter.TeamCityCiConfigurationProvider
import com.marmatsan.waterMyPlants.projectConfig.catalog.WaterMyPlantsDependencyDslCatalogProvider
import com.marmatsan.waterMyPlants.projectConfig.platform.HostOperatingSystem
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/** Maps Water My Plants identities and repository paths onto the reusable Figma extension. */
internal class WaterMyPlantsFigmaExtensionConfigurator(
    private val project: Project,
    private val writerConfig: FigmaWriterProjectConfig,
) {
    /** Applies the complete product-owned Figma configuration. */
    fun configure() {
        project.extensions.configure<figmaDocumentationSyncExtension> {
            designModelMetadataNodeUrl.set(
                "https://www.figma.com/design/${writerConfig.figmaFileKey}/Water-My-Plants" +
                    "?node-id=${writerConfig.metadataPageId.replace(
                        ':',
                        '-',
                    )}",
            )
            metadataNamespace.set(writerConfig.metadataNamespace)
            primaryCatalogModelName.set("waterMyPlants")
            dependencyCatalogProviderClassName.set(
                WaterMyPlantsDependencyDslCatalogProvider::class.java.name,
            )
            ciDocumentationEnabled.set(true)
            ciConfigurationModelName.set("teamCity")
            ciConfigurationProviderClassName.set(TeamCityCiConfigurationProvider::class.java.name)
            ciDefaultBranchAlias.set("<default>")
            versionsFile.set(
                project.layout.projectDirectory.file("repo/water-my-plants-project-config/versions.properties"),
            )
            ciExternalTopologyFile.set(project.layout.projectDirectory.file("docs/ci/external-topology.yaml"))
            ciWindowsRuntimeFile.set(project.layout.projectDirectory.file("docs/ci/windows-runtime.yaml"))
            ciGeneratedConfigurationDirectory.set(
                project.layout.projectDirectory.dir(".teamcity/target/generated-configs"),
            )
            changeImpactPolicyFile.set(
                project.layout.projectDirectory.file(
                    "repo/water-my-plants-project-config/water-my-plants/change-impact-policy.json",
                ),
            )
            toolsDirectory.set(project.layout.projectDirectory.dir("repo/figma-documentation-sync/tools"))
            ciConfigurationCommand.set(teamCityConfigurationCommand())

            includedBuilds.register("dependency-catalog") {
                modelName.set("dependencyCatalog")
                settingsFile.set(project.layout.projectDirectory.file("repo/dependency-catalog/settings.gradle.kts"))
                rootDirectory.set(project.layout.projectDirectory.dir("repo/dependency-catalog"))
                modulePathPrefix.set(":dependency-catalog")
                publishesCatalogs.set(false)
            }

            includedBuilds.register("figma-documentation-sync") {
                modelName.set("figmaDocumentationSync")
                settingsFile.set(
                    project.layout.projectDirectory.file("repo/figma-documentation-sync/settings.gradle.kts"),
                )
                rootDirectory.set(project.layout.projectDirectory.dir("repo/figma-documentation-sync"))
                modulePathPrefix.set(":figma-documentation-sync")
                publishesCatalogs.set(false)
            }

            includedBuilds.register("gradle-plugins") {
                modelName.set("gradlePlugins")
                settingsFile.set(project.layout.projectDirectory.file("repo/gradle-plugins/settings.gradle.kts"))
                rootDirectory.set(project.layout.projectDirectory.dir("repo/gradle-plugins"))
                modulePathPrefix.set(":gradle-plugins")
                publishesCatalogs.set(false)
                publishesConventionPlugins.set(true)
            }

            includedBuilds.register("unit-testing") {
                modelName.set("unitTesting")
                settingsFile.set(project.layout.projectDirectory.file("repo/unit-testing/settings.gradle.kts"))
                rootDirectory.set(project.layout.projectDirectory.dir("repo/unit-testing"))
                modulePathPrefix.set(":unit-testing")
                publishesCatalogs.set(false)
            }

            includedBuilds.register("verification-platform") {
                modelName.set("verificationPlatform")
                settingsFile.set(
                    project.layout.projectDirectory.file("repo/verification-platform/settings.gradle.kts"),
                )
                rootDirectory.set(project.layout.projectDirectory.dir("repo/verification-platform"))
                modulePathPrefix.set(":verification-platform")
                publishesCatalogs.set(false)
            }

            includedBuilds.register("water-my-plants-project-config") {
                modelName.set("waterMyPlantsProjectConfig")
                settingsFile.set(
                    project.layout.projectDirectory.file("repo/water-my-plants-project-config/settings.gradle.kts"),
                )
                rootDirectory.set(project.layout.projectDirectory.dir("repo/water-my-plants-project-config"))
                modulePathPrefix.set(":water-my-plants-project-config")
                publishesCatalogs.set(false)
            }
        }
    }

    private fun teamCityConfigurationCommand(): List<String> {
        val wrapper =
            project.layout.projectDirectory
                .file(if (HostOperatingSystem.isWindows) "mvnw.cmd" else "mvnw")
                .asFile
                .absolutePath
        val arguments =
            listOf(
                wrapper,
                "-f",
                project.layout.projectDirectory
                    .file(".teamcity/pom.xml")
                    .asFile.absolutePath,
                "teamcity-configs:generate",
            )

        return if (HostOperatingSystem.isWindows) {
            listOf(
                "cmd.exe",
                "/d",
                "/c",
            ) + arguments
        } else {
            arguments
        }
    }
}
