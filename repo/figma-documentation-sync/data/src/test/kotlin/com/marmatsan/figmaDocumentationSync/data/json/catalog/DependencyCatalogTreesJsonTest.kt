package com.marmatsan.figmaDocumentationSync.data.json.catalog

import com.marmatsan.figmaDocumentationSync.domain.model.catalog.CatalogVersion
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.DependencyCatalogTrees
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogEntry
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogNode
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogTree
import com.marmatsan.unitTest.dsl.given
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class DependencyCatalogTreesJsonTest :
    FunSpec(
        {
            test("round trips every catalog tree field") {
                given {
                    dependencyCatalogTrees()
                }.whenever { trees ->
                    DependencyCatalogTreesJson.decode(
                        DependencyCatalogTreesJson.encode(trees)
                    )
                }.then { expectedTrees, decodedTrees ->
                    decodedTrees shouldBe expectedTrees
                }
            }
        }
    )

private fun dependencyCatalogTrees(): DependencyCatalogTrees =
    DependencyCatalogTrees(
        libraries =
            LibraryCatalogTree(
                roots =
                    listOf(
                        LibraryCatalogNode(
                            group = "com",
                            entries =
                                listOf(
                                    LibraryCatalogEntry.Artifact(
                                        artifact = "example",
                                        version = CatalogVersion("exampleLibraryVersion"),
                                        requiredByModules = listOf(":app"),
                                        providedByConventionPlugins =
                                            listOf(
                                                LibraryCatalogEntry.ConventionPluginUsage(
                                                    pluginId = "com.example.library",
                                                    pluginModule = ":gradle-plugins:library",
                                                    requiredByModules = listOf(":feature")
                                                )
                                            ),
                                        configuredByConventionPlugins =
                                            listOf(
                                                LibraryCatalogEntry.ConventionPluginConfigurationUsage(
                                                    pluginId = "com.example.tool",
                                                    pluginModule = ":gradle-plugins:tool",
                                                    target = "tool.artifact"
                                                )
                                            )
                                    ),
                                    LibraryCatalogEntry.ArtifactsBundle(
                                        alias = "exampleBundle",
                                        artifacts =
                                            listOf(
                                                "example-core",
                                                "example-ui"
                                            ),
                                        version =
                                            CatalogVersion(
                                                "exampleBundleVersion",
                                                visible = false
                                            ),
                                        requiredByModules = listOf(":feature")
                                    )
                                ),
                            artifactsVisible = false,
                            children =
                                listOf(
                                    LibraryCatalogNode(
                                        group = "nested"
                                    )
                                )
                        )
                    )
            ),
        plugins =
            PluginCatalogTree(
                roots =
                    listOf(
                        PluginCatalogNode(
                            id = "com",
                            children =
                                listOf(
                                    PluginCatalogNode(
                                        id = "example",
                                        version = CatalogVersion("examplePluginVersion"),
                                        appliedToModules = listOf(":app"),
                                        providedByConventionPlugins =
                                            listOf(
                                                PluginCatalogNode.ConventionPluginUsage(
                                                    pluginId = "com.example.convention",
                                                    pluginModule = ":gradle-plugins:convention",
                                                    requiredByModules = listOf(":feature")
                                                )
                                            )
                                    )
                                )
                        )
                    )
            )
    )
