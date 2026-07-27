package com.marmatsan.figmaDocumentationSync.data.gradle.catalog

import com.marmatsan.figmaDocumentationSync.domain.model.catalog.CatalogVersion
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogEntry
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogNode
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogTree
import com.marmatsan.unitTest.dsl.given
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import java.io.File

internal class IncludedBuildSettingsCatalogReaderTest :
    FunSpec(
        {
            val temporaryDirectory =
                tempdir(
                    prefix = "included-build-settings-catalog-reader",
                )

            test("readLibraryTree maps included build settings libs catalog to library catalog tree") {
                given {
                    temporaryDirectory.settingsFile(
                        name = "libraries",
                        content =
                            """
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
                            """.trimIndent(),
                    )
                }.whenever { settingsFile ->
                    IncludedBuildSettingsCatalogReader().readLibraryTree(settingsFile)
                }.then { actualTree ->
                    actualTree shouldBe
                        LibraryCatalogTree(
                            roots =
                                listOf(
                                    LibraryCatalogNode(
                                        group = "io",
                                        children =
                                            listOf(
                                                LibraryCatalogNode(
                                                    group = "ktor",
                                                    entries =
                                                        listOf(
                                                            LibraryCatalogEntry.Artifact(
                                                                artifact = "ktor-bom",
                                                                version =
                                                                    CatalogVersion(
                                                                        value = "ktorLibraryVersion",
                                                                    ),
                                                            ),
                                                            LibraryCatalogEntry.Artifact(
                                                                artifact = "ktor-client-core",
                                                                version =
                                                                    CatalogVersion(
                                                                        value = null,
                                                                    ),
                                                            ),
                                                        ),
                                                ),
                                            ),
                                    ),
                                ),
                        )
                }
            }

            test("readLibraryTree returns an empty tree when the included build has no libs catalog") {
                given {
                    temporaryDirectory.settingsFile(
                        name = "without-libraries",
                        content =
                            """
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
                            """.trimIndent(),
                    )
                }.whenever { settingsFile ->
                    IncludedBuildSettingsCatalogReader().readLibraryTree(settingsFile)
                }.then { actualTree ->
                    actualTree shouldBe
                        LibraryCatalogTree(
                            roots = emptyList(),
                        )
                }
            }

            test("readPluginTree maps included build settings plugins catalog to plugin catalog tree") {
                given {
                    temporaryDirectory.settingsFile(
                        name = "plugins",
                        content =
                            """
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
                            """.trimIndent(),
                    )
                }.whenever { settingsFile ->
                    IncludedBuildSettingsCatalogReader().readPluginTree(settingsFile)
                }.then { actualTree ->
                    actualTree shouldBe
                        PluginCatalogTree(
                            roots =
                                listOf(
                                    PluginCatalogNode(
                                        id = "com",
                                        children =
                                            listOf(
                                                PluginCatalogNode(
                                                    id = "google",
                                                    children =
                                                        listOf(
                                                            PluginCatalogNode(
                                                                id = "devtools",
                                                                children =
                                                                    listOf(
                                                                        PluginCatalogNode(
                                                                            id = "ksp",
                                                                            version =
                                                                                CatalogVersion(
                                                                                    value = "kspPluginVersion",
                                                                                ),
                                                                        ),
                                                                    ),
                                                            ),
                                                        ),
                                                ),
                                            ),
                                    ),
                                ),
                        )
                }
            }

            test("readPluginTree returns an empty tree when the included build has no plugins catalog") {
                given {
                    temporaryDirectory.settingsFile(
                        name = "without-plugins",
                        content =
                            """
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
                            """.trimIndent(),
                    )
                }.whenever { settingsFile ->
                    IncludedBuildSettingsCatalogReader().readPluginTree(settingsFile)
                }.then { actualTree ->
                    actualTree shouldBe
                        PluginCatalogTree(
                            roots = emptyList(),
                        )
                }
            }
        },
    )

private fun File.settingsFile(
    name: String,
    content: String,
) = resolve("$name.settings.gradle.kts")
    .apply {
        writeText(content)
    }
