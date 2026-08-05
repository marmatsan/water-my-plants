package com.marmatsan.figmaDocumentationSync.plugin.generator.versions

import com.marmatsan.figmaDocumentationSync.domain.model.catalog.CatalogVersion
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogEntry
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogNode
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogTree
import com.marmatsan.figmaDocumentationSync.domain.model.versions.RepositoryVersionSection
import com.marmatsan.unitTest.dsl.given
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class VisuallyReferencedVersionSectionsSelectorTest :
    FunSpec(
        {
            test("select keeps only symbolic versions rendered by the production trees") {
                given {
                    SelectionFixture(
                        sections =
                            listOf(
                                RepositoryVersionSection(
                                    name = "Libraries",
                                    versions =
                                        mapOf(
                                            "visibleLibraryVersion" to "1.0.0",
                                            "hiddenLibraryVersion" to "2.0.0"
                                        )
                                ),
                                RepositoryVersionSection(
                                    name = "Plugins",
                                    versions =
                                        mapOf(
                                            "visiblePluginVersion" to "3.0.0",
                                            "repositoryToolingPluginVersion" to "4.0.0"
                                        )
                                )
                            ),
                        libraryTree =
                            LibraryCatalogTree(
                                roots =
                                    listOf(
                                        LibraryCatalogNode(
                                            group = "com",
                                            children =
                                                listOf(
                                                    LibraryCatalogNode(
                                                        group = "example",
                                                        entries =
                                                            listOf(
                                                                LibraryCatalogEntry.Artifact(
                                                                    artifact = "visible",
                                                                    version = CatalogVersion("visibleLibraryVersion")
                                                                ),
                                                                LibraryCatalogEntry.Artifact(
                                                                    artifact = "hidden",
                                                                    version =
                                                                        CatalogVersion(
                                                                            value = "hiddenLibraryVersion",
                                                                            visible = false
                                                                        )
                                                                )
                                                            )
                                                    )
                                                )
                                        )
                                    )
                            ),
                        pluginTree =
                            PluginCatalogTree(
                                roots =
                                    listOf(
                                        PluginCatalogNode(
                                            id = "com",
                                            children =
                                                listOf(
                                                    PluginCatalogNode(
                                                        id = "example.visible",
                                                        version = CatalogVersion("visiblePluginVersion")
                                                    )
                                                )
                                        )
                                    )
                            )
                    )
                }.whenever { fixture ->
                    VisuallyReferencedVersionSectionsSelector().select(
                        sections = fixture.sections,
                        libraryTree = fixture.libraryTree,
                        pluginTree = fixture.pluginTree
                    )
                }.then { selectedSections ->
                    selectedSections shouldBe
                        listOf(
                            RepositoryVersionSection(
                                name = "Libraries",
                                versions = mapOf("visibleLibraryVersion" to "1.0.0")
                            ),
                            RepositoryVersionSection(
                                name = "Plugins",
                                versions = mapOf("visiblePluginVersion" to "3.0.0")
                            )
                        )
                }
            }

            test("select preserves an empty section so stale Figma instances can be removed") {
                given {
                    RepositoryVersionSection(
                        name = "Plugins",
                        versions = mapOf("unusedPluginVersion" to "1.0.0")
                    )
                }.whenever { section ->
                    VisuallyReferencedVersionSectionsSelector().select(
                        sections = listOf(section),
                        libraryTree = LibraryCatalogTree(emptyList()),
                        pluginTree = PluginCatalogTree(emptyList())
                    )
                }.then { selectedSections ->
                    selectedSections shouldBe
                        listOf(
                            RepositoryVersionSection(
                                name = "Plugins",
                                versions = emptyMap()
                            )
                        )
                }
            }
        }
    )

private data class SelectionFixture(
    val sections: List<RepositoryVersionSection>,
    val libraryTree: LibraryCatalogTree,
    val pluginTree: PluginCatalogTree
)
