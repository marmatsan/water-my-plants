package com.marmatsan.dependencies.tree.node

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.marmatsan.dependencies.tree.mapper.toDependencyLibrary
import com.marmatsan.dependencies.tree.model.Artifact
import com.marmatsan.dependencies.tree.model.ArtifactsBundle
import com.marmatsan.dependencies.tree.model.Dependency
import com.marmatsan.dependencies.tree.model.DependencyNode
import com.marmatsan.dependencies.tree.model.LibraryEntry
import org.junit.jupiter.api.Test

internal class NodeTest {

    @Test
    fun `depthFirstPreOrderTraverse maps a given library tree preserving full paths and pre order`() {
        // GIVEN
        val androidx = Node(DependencyNode.Library("androidx"))
        val activity = Node(
            DependencyNode.Library(
                libraryGroup = "activity",
                entries = listOf(
                    LibraryEntry.Single(
                        Artifact(
                            artifact = "activity-compose",
                            version = "1.13.0"
                        )
                    )
                )
            )
        )
        val compose = Node(
            DependencyNode.Library(
                libraryGroup = "compose",
                entries = listOf(
                    LibraryEntry.Single(
                        Artifact(
                            artifact = "compose-bom",
                            version = "2026.05.00"
                        )
                    )
                )
            )
        )
        val ui = Node(
            DependencyNode.Library(
                libraryGroup = "ui",
                entries = listOf(
                    LibraryEntry.Bundle(
                        ArtifactsBundle(
                            alias = "composeBundle",
                            artifacts = listOf(
                                Artifact("ui"),
                                Artifact("ui-graphics"),
                                Artifact("ui-tooling")
                            )
                        )
                    )
                )
            )
        )
        val material3 = Node(
            DependencyNode.Library(
                libraryGroup = "material3",
                entries = listOf(
                    LibraryEntry.Single(
                        Artifact("material3")
                    )
                )
            )
        )

        androidx.add(activity)
        androidx.add(compose)
        compose.add(ui)
        compose.add(material3)

        // WHEN
        val actualLibraries = androidx.depthFirstPreOrderTraverse(
            pathSegment = DependencyNode.Library::libraryGroup,
            shouldIncludeNode = { it.entries != null },
            mapNode = { libraryNode, fullPath ->
                libraryNode.toDependencyLibrary(libraryGroup = fullPath)
            }
        )

        // THEN
        assertThat(actualLibraries).isEqualTo(
            listOf(
                Dependency.Library(
                    libraryGroup = "androidx.activity",
                    entries = listOf(
                        LibraryEntry.Single(
                            Artifact(
                                artifact = "activity-compose",
                                version = "1.13.0"
                            )
                        )
                    )
                ),
                Dependency.Library(
                    libraryGroup = "androidx.compose",
                    entries = listOf(
                        LibraryEntry.Single(
                            Artifact(
                                artifact = "compose-bom",
                                version = "2026.05.00"
                            )
                        )
                    )
                ),
                Dependency.Library(
                    libraryGroup = "androidx.compose.ui",
                    entries = listOf(
                        LibraryEntry.Bundle(
                            ArtifactsBundle(
                                alias = "composeBundle",
                                artifacts = listOf(
                                    Artifact("ui"),
                                    Artifact("ui-graphics"),
                                    Artifact("ui-tooling")
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
        )
    }
}
