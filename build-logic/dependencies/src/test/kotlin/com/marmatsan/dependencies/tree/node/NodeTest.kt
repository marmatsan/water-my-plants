package com.marmatsan.dependencies.tree.node

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.marmatsan.dependencies.tree.model.Artifact
import com.marmatsan.dependencies.tree.model.ArtifactsBundle
import com.marmatsan.dependencies.tree.model.DependencyNode
import com.marmatsan.dependencies.tree.model.LibraryEntry
import org.junit.jupiter.api.Test

internal class NodeTest {

    @Test
    fun `add appends child when children list is initialized`() {
        val root = Node(
            value = DependencyNode.Plugin(pluginId = "org.jetbrains.kotlin"),
            children = mutableListOf()
        )
        val child = Node(
            value = DependencyNode.Plugin(pluginId = "android", version = "2.3.0")
        )

        root.add(child)

        assertThat(root.children).isNotNull()
        assertThat(root.children.size).isEqualTo(1)
        assertThat(child).isEqualTo(root.children[0])
    }

    @Test
    fun `returns mapped results only for leaves and builds full path in pre-order`() {
        /* GIVEN */

        // root
        val root = Node(
            value = DependencyNode.Library(libraryGroup = "androidx"),
            children = mutableListOf()
        )

        // root -> activity (leaf)
        val activityLeaf = Node(
            value = DependencyNode.Library(
                libraryGroup = "activity",
                entries = listOf(
                    LibraryEntry.Single(
                        Artifact(artifact = "activity-compose", version = "1.9.1")
                    )
                )
            ),
            children = mutableListOf()
        )

        // root -> compose (non-leaf) -> ui (leaf)
        val composeBranch = Node(
            value = DependencyNode.Library(
                libraryGroup = "compose",
                entries = listOf(
                    LibraryEntry.Single(
                        Artifact(artifact = "compose-bom", version = "2024.09.00")
                    )
                )
            ),
            children = mutableListOf()
        )

        val uiLeaf = Node(
            value = DependencyNode.Library(
                libraryGroup = "ui",
                entries = listOf(
                    LibraryEntry.Bundle(
                        ArtifactsBundle(
                            alias = "composeBundle", artifacts = listOf(
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
            children = mutableListOf()
        )

        // root -> compose (non-leaf) -> material3 (leaf)
        val material3 = Node(
            value = DependencyNode.Library(
                libraryGroup = "material3",
                entries = listOf(
                    LibraryEntry.Single(
                        Artifact(artifact = "material3")
                    )
                )
            ),
            children = mutableListOf()
        )

        // Build the tree
        root.children.add(activityLeaf)
        root.children.add(composeBranch)
        composeBranch.children.add(uiLeaf)
        composeBranch.children.add(material3)

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

    @Test
    fun `depthFirstPreOrderTraverse visits matching nodes depth first and keeps sibling paths isolated`() {
        val root = Node(
            value = DependencyNode.Plugin(pluginId = "root")
        )
        val androidBranch = Node(
            value = DependencyNode.Plugin(pluginId = "android")
        )
        val applicationLeaf = Node(
            value = DependencyNode.Plugin(pluginId = "application", version = "8.10.0")
        )
        val kotlinBranch = Node(
            value = DependencyNode.Plugin(pluginId = "kotlin")
        )
        val androidLeaf = Node(
            value = DependencyNode.Plugin(pluginId = "android", version = "2.1.21")
        )
        val composeLeaf = Node(
            value = DependencyNode.Plugin(pluginId = "compose", version = "2.1.21")
        )

        root.add(androidBranch)
        root.add(kotlinBranch)
        androidBranch.add(applicationLeaf)
        kotlinBranch.add(androidLeaf)
        kotlinBranch.add(composeLeaf)

        val actualResults = root.depthFirstPreOrderTraverse(
            pathSegment = DependencyNode.Plugin::pluginId,
            nodeIsLeaf = { it.version != null },
            mapNode = { plugin, fullPath -> "${plugin.version}:$fullPath" }
        )

        assertThat(actualResults).isEqualTo(
            listOf(
                "8.10.0:root.android.application",
                "2.1.21:root.kotlin.android",
                "2.1.21:root.kotlin.compose"
            )
        )
    }
}