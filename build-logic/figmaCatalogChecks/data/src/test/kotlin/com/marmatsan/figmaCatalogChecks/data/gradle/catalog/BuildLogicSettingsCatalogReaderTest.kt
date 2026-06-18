package com.marmatsan.figmaCatalogChecks.data.gradle.catalog

import com.marmatsan.figmaCatalogChecks.domain.model.catalog.CatalogVersion
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.LibraryCatalogEntry
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.LibraryCatalogNode
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.PluginCatalogTree
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files

internal class BuildLogicSettingsCatalogReaderTest : FunSpec({

    test("readLibraryTree maps build logic settings libs catalog to library catalog tree") {
        // GIVEN
        val settingsFile = settingsFile(
            """
            dependencyResolutionManagement {
                versionCatalogs {
                    create("libs") {
                        library(
                            alias = "io.ktor.bom",
                            group = "io.ktor",
                            artifact = "ktor-bom"
                        ).version(version("ktorVersion"))

                        library(
                            alias = "io.ktor.client.core",
                            group = "io.ktor",
                            artifact = "ktor-client-core"
                        ).withoutVersion()
                    }
                }
            }
            """.trimIndent()
        )

        // WHEN
        val actualTree = BuildLogicSettingsCatalogReader().readLibraryTree(settingsFile)

        // THEN
        actualTree shouldBe LibraryCatalogTree(
            roots = listOf(
                LibraryCatalogNode(
                    group = "io",
                    children = listOf(
                        LibraryCatalogNode(
                            group = "ktor",
                            entries = listOf(
                                LibraryCatalogEntry.Artifact(
                                    artifact = "ktor-bom",
                                    version = CatalogVersion("ktorVersion")
                                ),
                                LibraryCatalogEntry.Artifact(
                                    artifact = "ktor-client-core",
                                    version = CatalogVersion(null)
                                )
                            )
                        )
                    )
                )
            )
        )
    }

    test("readPluginTree maps build logic settings plugins catalog to plugin catalog tree") {
        // GIVEN
        val settingsFile = settingsFile(
            """
            dependencyResolutionManagement {
                versionCatalogs {
                    create("plugins") {
                        plugin(
                            alias = "com.google.devtools.ksp",
                            id = "com.google.devtools.ksp"
                        ).version(version("kspVersion"))
                    }
                }
            }
            """.trimIndent()
        )

        // WHEN
        val actualTree = BuildLogicSettingsCatalogReader().readPluginTree(settingsFile)

        // THEN
        actualTree shouldBe PluginCatalogTree(
            roots = listOf(
                PluginCatalogNode(
                    id = "com",
                    children = listOf(
                        PluginCatalogNode(
                            id = "google",
                            children = listOf(
                                PluginCatalogNode(
                                    id = "devtools",
                                    children = listOf(
                                        PluginCatalogNode(
                                            id = "ksp",
                                            version = CatalogVersion("kspVersion")
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
})

private fun settingsFile(content: String) =
    Files.createTempFile("settings", ".gradle.kts").toFile().apply {
        writeText(content)
    }
