package com.marmatsan.figmaDesignSync.data.dependencies.catalog

import com.marmatsan.dependencies.tree.dsl.library.libraryTree
import com.marmatsan.dependencies.tree.dsl.plugin.pluginTree
import com.marmatsan.figmaDesignSync.data.gradle.catalog.GradleCatalogUsageReader
import com.marmatsan.figmaDesignSync.domain.model.catalog.CatalogVersion
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogEntry
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogNode
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaDesignSync.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaDesignSync.domain.model.catalog.PluginCatalogTree
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class DependenciesCatalogTreesReaderTest : FunSpec({

    test("readLibraryTree maps dependency library trees to catalog library trees") {
        // GIVEN
        val dependencyTree = libraryTree("androidx") {
            library("compose") {
                artifact(
                    artifact = "compose-bom",
                    version = "2026.05.01"
                )
                library("ui") {
                    artifactsBundle(
                        "ui",
                        "ui-tooling",
                        alias = "composeBundle"
                    )
                }
            }
        }

        // WHEN
        val actualTree = dependenciesCatalogTreesReader().readLibraryTree(listOf(dependencyTree))

        // THEN
        actualTree shouldBe LibraryCatalogTree(
            roots = listOf(
                LibraryCatalogNode(
                    group = "androidx",
                    children = listOf(
                        LibraryCatalogNode(
                            group = "compose",
                            entries = listOf(
                                LibraryCatalogEntry.Artifact(
                                    artifact = "compose-bom",
                                    version = CatalogVersion("2026.05.01")
                                )
                            ),
                            children = listOf(
                                LibraryCatalogNode(
                                    group = "ui",
                                    entries = listOf(
                                        LibraryCatalogEntry.ArtifactsBundle(
                                            alias = "composeBundle",
                                            artifacts = listOf(
                                                "ui",
                                                "ui-tooling"
                                            ),
                                            version = CatalogVersion(null)
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    }

    test("readPluginTree maps dependency plugin trees to catalog plugin trees") {
        // GIVEN
        val dependencyTree = pluginTree("org") {
            plugin("jetbrains") {
                plugin("kotlin") {
                    plugin(
                        id = "android",
                        version = "2.4.0"
                    )
                }
            }
        }

        // WHEN
        val actualTree = dependenciesCatalogTreesReader().readPluginTree(listOf(dependencyTree))

        // THEN
        actualTree shouldBe PluginCatalogTree(
            roots = listOf(
                PluginCatalogNode(
                    id = "org",
                    children = listOf(
                        PluginCatalogNode(
                            id = "jetbrains",
                            children = listOf(
                                PluginCatalogNode(
                                    id = "kotlin",
                                    children = listOf(
                                        PluginCatalogNode(
                                            id = "android",
                                            version = CatalogVersion("2.4.0")
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    }

    test("readLibraryTreeWithVersionAliases maps versions as property aliases") {
        // WHEN
        val actualTree = dependenciesCatalogTreesReader().readLibraryTreeWithVersionAliases(rootDir = java.io.File("."))

        // THEN
        val activity = actualTree.roots
            .first { node -> node.group == "androidx" }
            .children
            .first { node -> node.group == "activity" }
            .entries
            .single() as LibraryCatalogEntry.Artifact

        activity.version shouldBe CatalogVersion("activityComposeVersion")
    }
})

private fun dependenciesCatalogTreesReader() =
    DependenciesCatalogTreesReader(
        gradleCatalogUsageReader = GradleCatalogUsageReader()
    )
