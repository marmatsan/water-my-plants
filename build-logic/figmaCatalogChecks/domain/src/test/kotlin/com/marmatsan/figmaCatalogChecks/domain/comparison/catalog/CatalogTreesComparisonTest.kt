package com.marmatsan.figmaCatalogChecks.domain.comparison.catalog

import com.marmatsan.figmaCatalogChecks.domain.model.catalog.CatalogVersion
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.LibraryCatalogEntry
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.LibraryCatalogNode
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.PluginCatalogTree
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

internal class CatalogTreesComparisonTest : FunSpec({

    test("library comparison returns matching result when repository and Figma trees are equal") {
        // GIVEN
        val tree = LibraryCatalogTree(
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
                            )
                        )
                    )
                )
            )
        )

        // WHEN
        val result = LibraryCatalogTreeComparison().compare(
            repositoryTree = tree,
            figmaTree = tree
        )

        // THEN
        result.matches shouldBe true
    }

    test("library comparison reports missing entries in Figma") {
        // GIVEN
        val repositoryTree = LibraryCatalogTree(
            roots = listOf(
                LibraryCatalogNode(
                    group = "androidx",
                    entries = listOf(
                        LibraryCatalogEntry.Artifact(
                            artifact = "activity-compose",
                            version = CatalogVersion("1.12.0")
                        )
                    )
                )
            )
        )
        val figmaTree = LibraryCatalogTree(
            roots = listOf(
                LibraryCatalogNode(group = "androidx")
            )
        )

        // WHEN
        val result = LibraryCatalogTreeComparison().compare(
            repositoryTree = repositoryTree,
            figmaTree = figmaTree
        )

        // THEN
        result.matches shouldBe false
        result.missingInFigma shouldContain (
            "library androidx artifact activity-compose" to "version=1.12.0"
        )
        result.report() shouldContain "Missing in Figma"
    }

    test("library comparison reports changed bundle values") {
        // GIVEN
        val repositoryTree = LibraryCatalogTree(
            roots = listOf(
                LibraryCatalogNode(
                    group = "androidx",
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
        val figmaTree = LibraryCatalogTree(
            roots = listOf(
                LibraryCatalogNode(
                    group = "androidx",
                    entries = listOf(
                        LibraryCatalogEntry.ArtifactsBundle(
                            alias = "composeBundle",
                            artifacts = listOf("ui"),
                            version = CatalogVersion(null)
                        )
                    )
                )
            )
        )

        // WHEN
        val result = LibraryCatalogTreeComparison().compare(
            repositoryTree = repositoryTree,
            figmaTree = figmaTree
        )

        // THEN
        result.matches shouldBe false
        result.changedValues shouldContain (
            "library androidx bundle composeBundle" to CatalogTreeDifference(
                repositoryValue = "artifacts=[ui, ui-tooling]; version=hidden",
                figmaValue = "artifacts=[ui]; version=hidden"
            )
        )
        result.report() shouldContain "Changed values"
    }

    test("plugin comparison reports extra plugins in Figma") {
        // GIVEN
        val repositoryTree = PluginCatalogTree(
            roots = listOf(
                PluginCatalogNode(id = "com")
            )
        )
        val figmaTree = PluginCatalogTree(
            roots = listOf(
                PluginCatalogNode(
                    id = "com",
                    children = listOf(
                        PluginCatalogNode(
                            id = "android",
                            version = CatalogVersion("androidGradlePlugin")
                        )
                    )
                )
            )
        )

        // WHEN
        val result = PluginCatalogTreeComparison().compare(
            repositoryTree = repositoryTree,
            figmaTree = figmaTree
        )

        // THEN
        result.matches shouldBe false
        result.extraInFigma shouldContain (
            "plugin com.android" to "version=androidGradlePlugin"
        )
        result.report() shouldContain "Extra in Figma"
    }

    test("plugin comparison reports changed plugin values") {
        // GIVEN
        val repositoryTree = PluginCatalogTree(
            roots = listOf(
                PluginCatalogNode(
                    id = "org",
                    version = CatalogVersion("kotlinVersion")
                )
            )
        )
        val figmaTree = PluginCatalogTree(
            roots = listOf(
                PluginCatalogNode(
                    id = "org",
                    version = CatalogVersion("otherKotlinVersion")
                )
            )
        )

        // WHEN
        val result = PluginCatalogTreeComparison().compare(
            repositoryTree = repositoryTree,
            figmaTree = figmaTree
        )

        // THEN
        result.matches shouldBe false
        result.changedValues shouldContain (
            "plugin org" to CatalogTreeDifference(
                repositoryValue = "version=kotlinVersion",
                figmaValue = "version=otherKotlinVersion"
            )
        )
    }
})
