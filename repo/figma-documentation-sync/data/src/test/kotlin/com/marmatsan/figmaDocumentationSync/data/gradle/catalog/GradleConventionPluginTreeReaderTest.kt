package com.marmatsan.figmaDocumentationSync.data.gradle.catalog

import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogTree
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files

internal class GradleConventionPluginTreeReaderTest :
    FunSpec(
        {

            test("readPluginTree detects any Gradle convention plugin build file under included build root") {
                // GIVEN
                val rootDir = Files.createTempDirectory("gradle-convention-plugin-tree").toFile()
                val includedBuildRootDir =
                    rootDir.resolve(
                        relative = "repo/gradle-plugins",
                    )
                includedBuildRootDir.writeBuildFile(
                    path = "analytics",
                    content =
                        conventionPluginBuildFile(
                            pluginName = "com.marmatsan.analytics",
                            implementationClass = "\${pluginName}.plugin.AnalyticsGradleConventionPlugin",
                        ),
                )
                includedBuildRootDir.writeBuildFile(
                    path = "sync/plugin",
                    content =
                        conventionPluginBuildFile(
                            pluginName = "com.marmatsan.figmaDocumentationSync",
                            implementationClass = "\${pluginName}.plugin.gradle.FigmaDocumentationSyncGradlePlugin",
                        ),
                )
                includedBuildRootDir.writeBuildFile(
                    path = "reporting",
                    content =
                        literalIdConventionPluginBuildFile(
                            pluginId = "com.marmatsan.reporting",
                        ),
                )
                includedBuildRootDir.writeBuildFile(
                    path = "dependencies",
                    content =
                        conventionPluginBuildFile(
                            pluginName = "com.marmatsan.dependencies",
                            implementationClass = "\${pluginName}.plugin.DependenciesPlugin",
                        ),
                )

                // WHEN
                val actualTree =
                    GradleConventionPluginTreeReader().readPluginTree(
                        rootDir = includedBuildRootDir,
                        usageByPluginId =
                            mapOf(
                                "com.marmatsan.analytics" to
                                    setOf(
                                        ":app",
                                        ":core:ui",
                                    ),
                                "com.marmatsan.reporting" to setOf(":app"),
                            ),
                    )

                // THEN
                actualTree shouldBe
                    PluginCatalogTree(
                        roots =
                            listOf(
                                PluginCatalogNode(
                                    id = "com",
                                    children =
                                        listOf(
                                            PluginCatalogNode(
                                                id = "marmatsan",
                                                children =
                                                    listOf(
                                                        PluginCatalogNode(
                                                            id = "analytics",
                                                            appliedToModules =
                                                                listOf(
                                                                    ":app",
                                                                    ":core:ui",
                                                                ),
                                                        ),
                                                        PluginCatalogNode(
                                                            id = "reporting",
                                                            appliedToModules = listOf(":app"),
                                                        ),
                                                    ),
                                            ),
                                        ),
                                ),
                            ),
                    )
            }
        },
    )

private fun File.writeBuildFile(
    path: String,
    content: String,
) {
    val directory =
        resolve(
            relative = path,
        )
    directory.mkdirs()
    directory
        .resolve(
            relative = "build.gradle.kts",
        ).writeText(content)
}

private fun conventionPluginBuildFile(
    pluginName: String,
    implementationClass: String,
): String =
    """
    plugins {
        `kotlin-dsl`
        `java-gradle-plugin`
    }

    gradlePlugin {
        val pluginName = "$pluginName"
        plugins.register(pluginName) {
            id = pluginName
            implementationClass = "$implementationClass"
        }
    }
    """.trimIndent()

private fun literalIdConventionPluginBuildFile(
    pluginId: String,
): String =
    """
    plugins {
        `kotlin-dsl`
        `java-gradle-plugin`
    }

    gradlePlugin {
        plugins.register("reporting") {
            id = "$pluginId"
            implementationClass = "$pluginId.plugin.ReportingGradleConventionPlugin"
        }
    }
    """.trimIndent()
