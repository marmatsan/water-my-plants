package com.marmatsan.dependencies.tree.node

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.marmatsan.dependencies.tree.library.libraryTree
import com.marmatsan.dependencies.tree.model.Artifact
import com.marmatsan.dependencies.tree.model.ArtifactsBundle
import com.marmatsan.dependencies.tree.model.Dependency
import com.marmatsan.dependencies.tree.model.LibraryEntry
import com.marmatsan.dependencies.tree.plugin.pluginTree
import org.junit.jupiter.api.Test

internal class NodeExtensionTest {

    @Test
    fun `getLibraries() transforms correctly the libraries tree to a dependencies list`() {
        // GIVEN
        val actualLibrariesTree = libraryTree("androidx") {
            library("activity") {
                artifact(
                    "activity-compose",
                    version = "1.9.1"
                )
            }
            library("compose") {
                artifact(
                    "compose-bom",
                    version = "2025.06.01"
                )
                library("ui") {
                    artifactsBundle(
                        "ui",
                        "ui-graphics",
                        "ui-tooling",
                        "ui-tooling-preview",
                        alias = "composeBundle"
                    )
                }
                library("material3") {
                    artifact(
                        "material3"
                    )
                }
            }
        }

        // WHEN
        val actualLibraries = actualLibrariesTree.toDependencyLibraries()

        val expectedLibraries = listOf(
            Dependency.Library(
                libraryGroup = "androidx.activity",
                entries = listOf(
                    LibraryEntry.Single(
                        Artifact("activity-compose", "1.9.1")
                    )
                )
            ),
            Dependency.Library(
                libraryGroup = "androidx.compose",
                entries = listOf(
                    LibraryEntry.Single(
                        Artifact("compose-bom", "2025.06.01")
                    )
                )
            ),
            Dependency.Library(
                libraryGroup = "androidx.compose.ui",
                entries = listOf(
                    LibraryEntry.Bundle(
                        artifactsBundle = ArtifactsBundle(
                            alias = "composeBundle",
                            artifacts = listOf(
                                Artifact(
                                    artifact = "ui"
                                ),
                                Artifact(
                                    artifact = "ui-graphics"
                                ),
                                Artifact(
                                    artifact = "ui-tooling"
                                ),
                                Artifact(
                                    artifact = "ui-tooling-preview"
                                )
                            )
                        )
                    )
                )
            ),
            Dependency.Library(
                libraryGroup = "androidx.compose.material3",
                entries = listOf(
                    LibraryEntry.Single(
                        Artifact("material3")
                    )
                )
            )
        )

        // THEN
        assertThat(actualLibraries).isEqualTo(expectedLibraries)
    }

    @Test
    fun `getPlugins() transforms correctly the libraries tree to a plugins list`() {
        // GIVEN
        val pluginsTree = pluginTree("org") {
            plugin("jetbrains") {
                plugin("kotlin") {
                    plugin("android", version = "2.1.21")
                    plugin("plugin") {
                        plugin("compose", version = "2.1.21")
                    }
                }
            }
        }

        // WHEN
        val actualPlugins = pluginsTree.toDependencyPlugins()

        val expectedPlugins = listOf(
            Dependency.Plugin(
                pluginId = "org.jetbrains.kotlin.android",
                version = "2.1.21"
            ),
            Dependency.Plugin(
                pluginId = "org.jetbrains.kotlin.plugin.compose",
                version = "2.1.21"
            )
        )

        // THEN
        assertThat(actualPlugins).isEqualTo(expectedPlugins)
    }
}