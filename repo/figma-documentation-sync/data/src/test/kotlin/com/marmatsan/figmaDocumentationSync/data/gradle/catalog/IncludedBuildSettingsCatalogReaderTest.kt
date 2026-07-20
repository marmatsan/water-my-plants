package com.marmatsan.figmaDocumentationSync.data.gradle.catalog

import com.marmatsan.figmaDocumentationSync.domain.model.catalog.CatalogVersion
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogEntry
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogNode
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogTree
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files

internal class IncludedBuildSettingsCatalogReaderTest : FunSpec(
    {

    test("readLibraryTree maps included build settings libs catalog to library catalog tree") {
        // GIVEN
        val settingsFile = settingsFile(
            content = """
            dependencyResolutionManagement {
                versionCatalogs {
                    create("libs") {
                        library(
                            alias = "io.ktor.bom",
                            group = "io.ktor",
                            artifact = "ktor-bom"
                        ).version(version("ktorLibraryVersion"))

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
        val actualTree = IncludedBuildSettingsCatalogReader().readLibraryTree(settingsFile)

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
                                    version = CatalogVersion(
                                        value = "ktorLibraryVersion"
                                    )
                                ),
                                LibraryCatalogEntry.Artifact(
                                    artifact = "ktor-client-core",
                                    version = CatalogVersion(
                                        value = null
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    }

    test("readLibraryTree returns an empty tree when the included build has no libs catalog") {
        // GIVEN
        val settingsFile = settingsFile(
            content = """
            dependencyResolutionManagement {
                versionCatalogs {
                    create("plugins") {
                        plugin(
                            alias = "org.jetbrains.dokka",
                            id = "org.jetbrains.dokka"
                        ).version(version("dokkaPluginVersion"))
                    }
                }
            }
            """.trimIndent()
        )

        // WHEN
        val actualTree = IncludedBuildSettingsCatalogReader().readLibraryTree(settingsFile)

        // THEN
        actualTree shouldBe LibraryCatalogTree(
            roots = emptyList()
        )
    }

    test("readPluginTree maps included build settings plugins catalog to plugin catalog tree") {
        // GIVEN
        val settingsFile = settingsFile(
            content = """
            dependencyResolutionManagement {
                versionCatalogs {
                    create("plugins") {
                        plugin(
                            alias = "com.google.devtools.ksp",
                            id = "com.google.devtools.ksp"
                        ).version(version("kspPluginVersion"))
                    }
                }
            }
            """.trimIndent()
        )

        // WHEN
        val actualTree = IncludedBuildSettingsCatalogReader().readPluginTree(settingsFile)

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
                                            version = CatalogVersion(
                                                value = "kspPluginVersion"
                                            )
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

    test("readPluginTree returns an empty tree when the included build has no plugins catalog") {
        // GIVEN
        val settingsFile = settingsFile(
            content = """
            dependencyResolutionManagement {
                versionCatalogs {
                    create("libs") {
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
        val actualTree = IncludedBuildSettingsCatalogReader().readPluginTree(settingsFile)

        // THEN
        actualTree shouldBe PluginCatalogTree(
            roots = emptyList()
        )
    }
}
)

private fun settingsFile(
    content: String
) =
    Files.createTempFile(
        "settings",
        ".gradle.kts"
    ).toFile().apply {
        writeText(content)
    }
