package com.marmatsan.figmaDocumentationSync.data.datasource.catalog

import com.marmatsan.figmaDocumentationSync.data.gradle.catalog.GradleConventionPluginTreeReader
import com.marmatsan.figmaDocumentationSync.data.gradle.catalog.GradleIncludedBuildCatalogUsageReader
import com.marmatsan.figmaDocumentationSync.data.gradle.catalog.GradleMainCatalogUsageReader
import com.marmatsan.figmaDocumentationSync.data.gradle.catalog.GradlePluginTreeReader
import com.marmatsan.figmaDocumentationSync.data.gradle.catalog.IncludedBuildSettingsCatalogReader
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaDocumentationSync.domain.port.catalog.ProjectCatalogTreeSource
import com.marmatsan.figmaDocumentationSync.domain.port.gradle.IncludedBuildSource
import com.marmatsan.unitTest.dsl.given
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import java.io.File

internal class ProjectCatalogTreesDataSourceTest :
    FunSpec(
        {
            val temporaryDirectory =
                tempdir(
                    prefix = "project-catalog-trees-data-source"
                )

            test("custom convention plugin tree maps type-safe aliases to applying modules") {
                given {
                    val rootDir = temporaryDirectory.resolve("convention-plugin-project").apply { mkdirs() }
                    rootDir.writeBuildFile(
                        path = "app",
                        content =
                            """
                            plugins {
                                alias(plugins.plugins.com.marmatsan.android)
                                alias(plugins.plugins.com.marmatsan.compose)
                            }
                            """.trimIndent()
                    )
                    rootDir.writeBuildFile(
                        path = "core/ui",
                        content =
                            """
                            plugins {
                                alias(plugins.plugins.com.marmatsan.android) apply false
                            }
                            """.trimIndent()
                    )
                    val includedBuild = rootDir.resolve("repo/gradle-plugins").apply { mkdirs() }
                    includedBuild.resolve("settings.gradle.kts").writeText("rootProject.name = \"gradle-plugins\"")
                    includedBuild.writeGradlePluginBuildFile(
                        path = "android",
                        pluginId = "com.marmatsan.android",
                        implementationClass = "com.marmatsan.android.plugin.AndroidGradleConventionPlugin"
                    )
                    includedBuild.writeGradlePluginBuildFile(
                        path = "compose",
                        pluginId = "com.marmatsan.compose",
                        implementationClass = "com.marmatsan.compose.plugin.ComposeGradleConventionPlugin"
                    )
                    rootDir to includedBuild
                }.whenever { (rootDir, includedBuild) ->
                    dataSource().readPluginTree(
                        ProjectCatalogTreeSource.CustomGradleConventionPlugins(
                            rootDirPath = rootDir.absolutePath,
                            includedBuilds =
                                listOf(
                                    IncludedBuildSource(
                                        settingsFilePath =
                                            includedBuild.resolve("settings.gradle.kts").absolutePath,
                                        rootDirPath = includedBuild.absolutePath,
                                        modulePathPrefix = ":gradle-plugins",
                                        publishesCatalogs = false,
                                        publishesConventionPlugins = true
                                    )
                                )
                        )
                    )
                }.then { tree ->
                    tree.findPlugin("com.marmatsan.android").appliedToModules shouldBe listOf(":app")
                    tree.findPlugin("com.marmatsan.compose").appliedToModules shouldBe listOf(":app")
                }
            }

            test("custom Gradle plugin tree maps root aliases and literal ids to the root module") {
                given {
                    val rootDir = temporaryDirectory.resolve("regular-plugin-project").apply { mkdirs() }
                    rootDir.writeBuildFile(
                        path = "",
                        content =
                            """
                            plugins {
                                alias(toolPlugins.plugins.com.marmatsan.verificationPlatform)
                                id("com.marmatsan.waterMyPlantsProjectConfig")
                            }
                            """.trimIndent()
                    )
                    rootDir.writeRegularIncludedBuildPlugin(
                        buildPath = "repo/verification-platform",
                        modulePath = "plugin",
                        pluginId = "com.marmatsan.verificationPlatform"
                    )
                    rootDir.writeRegularIncludedBuildPlugin(
                        buildPath = "repo/water-my-plants-project-config",
                        modulePath = "plugin",
                        pluginId = "com.marmatsan.waterMyPlantsProjectConfig"
                    )
                    rootDir
                }.whenever { rootDir ->
                    dataSource().readPluginTree(
                        ProjectCatalogTreeSource.CustomGradlePlugins(
                            rootDirPath = rootDir.absolutePath
                        )
                    )
                }.then { tree ->
                    tree.findPlugin("com.marmatsan.verificationPlatform").appliedToModules shouldBe listOf(":")
                    tree.findPlugin("com.marmatsan.waterMyPlantsProjectConfig").appliedToModules shouldBe listOf(":")
                }
            }
        }
    )

private fun dataSource(): ProjectCatalogTreesDataSource =
    ProjectCatalogTreesDataSource(
        includedBuildSettingsCatalogReader = IncludedBuildSettingsCatalogReader(),
        includedBuildCatalogUsageReader = GradleIncludedBuildCatalogUsageReader(),
        mainCatalogUsageReader = GradleMainCatalogUsageReader(),
        gradleConventionPluginTreeReader = GradleConventionPluginTreeReader(),
        gradlePluginTreeReader = GradlePluginTreeReader()
    )

private fun File.writeRegularIncludedBuildPlugin(
    buildPath: String,
    modulePath: String,
    pluginId: String
) {
    val includedBuild = resolve(buildPath).apply { mkdirs() }
    includedBuild.resolve("settings.gradle.kts").writeText("rootProject.name = \"${includedBuild.name}\"")
    includedBuild.writeGradlePluginBuildFile(
        path = modulePath,
        pluginId = pluginId,
        implementationClass = "$pluginId.plugin.RepositoryGradlePlugin"
    )
}

private fun File.writeGradlePluginBuildFile(
    path: String,
    pluginId: String,
    implementationClass: String
) {
    writeBuildFile(
        path = path,
        content =
            """
            gradlePlugin {
                val pluginName = "$pluginId"
                plugins.register(pluginName) {
                    id = pluginName
                    implementationClass = "$implementationClass"
                }
            }
            """.trimIndent()
    )
}

private fun File.writeBuildFile(
    path: String,
    content: String
) {
    val directory = resolve(path).apply { mkdirs() }
    directory.resolve("build.gradle.kts").writeText(content)
}

private fun com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogTree.findPlugin(
    pluginId: String
): PluginCatalogNode {
    val segments = pluginId.split(".")
    return segments.drop(1).fold(
        initial = roots.first { node -> node.id == segments.first() }
    ) { node, segment ->
        node.children.first { child -> child.id == segment }
    }
}
