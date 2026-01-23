package com.marmatsan.dependencies.tree.tree

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.marmatsan.dependencies.tree.model.NodeData
import org.junit.jupiter.api.Test

internal class TreeNodeTest {

    @Test
    fun `add appends child when children list is initialized`() {
        val root = TreeNode(
            data = NodeData.Plugin(pluginId = "org.jetbrains.kotlin"),
            children = mutableListOf()
        )
        val child = TreeNode(
            data = NodeData.Plugin(pluginId = "android", version = "2.3.0")
        )

        root.add(child)

        assertThat(root.children).isNotNull()
        assertThat(root.children!!.size).isEqualTo(1)
        assertThat(child).isEqualTo(root.children!![0])
    }

    @Test
    fun `returns mapped results only for leaves and builds full path in pre-order`() {
        /* GIVEN */

        // root
        val root = TreeNode(
            data = NodeData.Library(libraryGroup = "androidx"),
            children = mutableListOf()
        )

        // root -> activity (leaf)
        val activityLeaf = TreeNode(
            data = NodeData.Library(
                libraryGroup = "activity",
                entries = listOf(
                    NodeData.Library.Entry.Single(
                        NodeData.Library.Artifact(artifact = "activity-compose", version = "1.9.1")
                    )
                )
            ),
            children = mutableListOf()
        )

        // root -> compose (non-leaf) -> ui (leaf)
        val composeBranch = TreeNode(
            data = NodeData.Library(
                libraryGroup = "compose",
                entries = listOf(
                    NodeData.Library.Entry.Single(
                        NodeData.Library.Artifact(artifact = "compose-bom", version = "2024.09.00")
                    )
                )
            ),
            children = mutableListOf()
        )

        val uiLeaf = TreeNode(
            data = NodeData.Library(
                libraryGroup = "ui",
                entries = listOf(
                    NodeData.Library.Entry.Bundle(
                        NodeData.Library.ArtifactsBundle(
                            alias = "composeBundle", artifacts = listOf(
                                NodeData.Library.Artifact(
                                    artifact = "ui"
                                ),
                                NodeData.Library.Artifact(
                                    artifact = "ui-graphics"
                                ),
                                NodeData.Library.Artifact(
                                    artifact = "ui-tooling"
                                ),
                                NodeData.Library.Artifact(
                                    artifact = "ui-tooling-preview"
                                )
                            )
                        )
                    )
                )
            ),
            children = mutableListOf()
        )

        // root -> compose (non-leaf) -> material3 (leaf)
        val material3 = TreeNode(
            data = NodeData.Library(
                libraryGroup = "material3",
                entries = listOf(
                    NodeData.Library.Entry.Single(
                        NodeData.Library.Artifact(artifact = "material3")
                    )
                )
            ),
            children = mutableListOf()
        )

        // Build the tree
        root.children!!.add(activityLeaf)
        root.children!!.add(composeBranch)
        composeBranch.children!!.add(uiLeaf)
        composeBranch.children!!.add(material3)

        /* WHEN */

        // We map to the computed full path string so we can assert traversal correctness.
        val actualResults = root.depthFirstPreOrderTraverse(
            pathSegment = { it.libraryGroup },
            nodeIsLeaf = { it.entries != null },
            mapNode = { _, fullPath -> fullPath }
        )

        /* THEN */

        // Pre-order leaf visitation order should be:
        val expectedResults = listOf(
            "androidx.activity",
            "androidx.compose",
            "androidx.compose.ui",
            "androidx.compose.material3"
        )

        assertThat(actualResults).isEqualTo(expectedResults)
    }
}