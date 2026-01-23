package com.marmatsan.dependencies.tree.library

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.marmatsan.dependencies.tree.model.NodeData
import com.marmatsan.dependencies.tree.tree.TreeNode
import org.junit.jupiter.api.Test

internal class LibrariesDslTest {

    @Test
    fun `librariesDsl generates the expected tree`() {
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
        val expectedLibrariesTree = TreeNode(
            NodeData.Library("androidx"),
            children = mutableListOf(
                TreeNode(
                    NodeData.Library(
                        "activity", listOf(
                            NodeData.Library.Entry.Single(
                                NodeData.Library.Artifact("activity-compose", "1.9.1")
                            )
                        )
                    )
                ),
                TreeNode(
                    NodeData.Library(
                        "compose", listOf(
                            NodeData.Library.Entry.Single(
                                NodeData.Library.Artifact("compose-bom", "2025.06.01")
                            )
                        )
                    ),
                    children = mutableListOf(
                        TreeNode(
                            NodeData.Library(
                                "ui", listOf(
                                    NodeData.Library.Entry.Bundle(
                                        NodeData.Library.ArtifactsBundle(
                                            alias = "composeBundle",
                                            artifacts = listOf(
                                                NodeData.Library.Artifact("ui", null),
                                                NodeData.Library.Artifact("ui-graphics", null),
                                                NodeData.Library.Artifact("ui-tooling", null),
                                                NodeData.Library.Artifact("ui-tooling-preview", null)
                                            ),
                                            version = null
                                        )
                                    )
                                )
                            )
                        ),
                        TreeNode(
                            NodeData.Library(
                                "material3", listOf(
                                    NodeData.Library.Entry.Single(
                                        NodeData.Library.Artifact("material3", null)
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )

        // THEN
        assertThat(actualLibrariesTree).isEqualTo(expectedLibrariesTree)
    }
}