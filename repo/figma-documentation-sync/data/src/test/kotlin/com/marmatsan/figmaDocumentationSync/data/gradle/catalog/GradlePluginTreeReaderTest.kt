package com.marmatsan.figmaDocumentationSync.data.gradle.catalog

import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogTree
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import java.io.File

internal class GradlePluginTreeReaderTest :
    FunSpec(
        {
            val temporaryDirectory =
                tempdir(
                    prefix = "gradle-plugin-tree-reader",
                )

            test("readPluginTree detects regular Gradle plugins and ignores convention plugins") {
                // GIVEN
                val rootDir = temporaryDirectory.resolve("project").apply { mkdirs() }
                rootDir.writeSettingsFile(
                    path = "repo/gradle-plugins",
                )
                rootDir.writeSettingsFile(
                    path = "repo/figma-documentation-sync",
                )
                rootDir.writeGradlePluginBuildFile(
                    path = "repo/figma-documentation-sync/plugin",
                    pluginName = "com.marmatsan.figmaDocumentationSync",
                    implementationClass = "\${pluginName}.plugin.gradle.FigmaDocumentationSyncGradlePlugin",
                )
                rootDir.writeGradlePluginBuildFile(
                    path = "repo/gradle-plugins/analytics",
                    pluginName = "com.marmatsan.analytics",
                    implementationClass = "\${pluginName}.plugin.AnalyticsGradlePlugin",
                )
                rootDir.writeGradlePluginBuildFile(
                    path = "repo/gradle-plugins/android",
                    pluginName = "com.marmatsan.android",
                    implementationClass = "\${pluginName}.plugin.AndroidGradleConventionPlugin",
                )

                // WHEN
                val actualTree =
                    GradlePluginTreeReader().readPluginTree(
                        rootDir = rootDir,
                        includedPluginIds = setOf("com.marmatsan.figmaDocumentationSync"),
                        usageByPluginId = emptyMap(),
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
                                                            id = "figmaDocumentationSync",
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

private fun File.writeSettingsFile(
    path: String,
) {
    val directory =
        resolve(
            relative = path,
        )
    directory.mkdirs()
    directory
        .resolve(
            relative = "settings.gradle.kts",
        ).writeText("rootProject.name = \"${directory.name}\"")
}

private fun File.writeGradlePluginBuildFile(
    path: String,
    pluginName: String,
    implementationClass: String,
) {
    val directory =
        resolve(
            relative = path,
        )
    directory.mkdirs()
    directory
        .resolve(
            relative = "build.gradle.kts",
        ).writeText(
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
            """.trimIndent(),
        )
}
