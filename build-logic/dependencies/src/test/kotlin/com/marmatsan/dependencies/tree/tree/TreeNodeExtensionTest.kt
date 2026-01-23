package com.marmatsan.dependencies.tree.tree

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.marmatsan.dependencies.tree.library.libraryTree
import com.marmatsan.dependencies.tree.model.Dependency
import com.marmatsan.dependencies.tree.plugin.pluginTree
import org.junit.jupiter.api.Test

internal class TreeNodeExtensionTest {

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
        val actualLibraries = actualLibrariesTree.getLibraries()

        val expectedLibraries = listOf(
            Dependency.Library(
                libraryGroup = "androidx.activity",
                entries = listOf(
                    Dependency.Library.Entry.Single(
                        Dependency.Library.Artifact("activity-compose", "1.9.1")
                    )
                )
            ),
            Dependency.Library(
                libraryGroup = "androidx.compose",
                entries = listOf(
                    Dependency.Library.Entry.Single(
                        Dependency.Library.Artifact("compose-bom", "2025.06.01")
                    )
                )
            ),
            Dependency.Library(
                libraryGroup = "androidx.compose.ui",
                entries = listOf(
                    Dependency.Library.Entry.Bundle(
                        artifactsBundle = Dependency.Library.ArtifactsBundle(
                            alias = "composeBundle",
                            artifacts = listOf(
                                Dependency.Library.Artifact(
                                    artifact = "ui"
                                ),
                                Dependency.Library.Artifact(
                                    artifact = "ui-graphics"
                                ),
                                Dependency.Library.Artifact(
                                    artifact = "ui-tooling"
                                ),
                                Dependency.Library.Artifact(
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
                    Dependency.Library.Entry.Single(
                        Dependency.Library.Artifact("material3")
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
        val actualPlugins = pluginsTree.getPlugins()

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