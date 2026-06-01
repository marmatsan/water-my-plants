package com.marmatsan.dependencies.tree.library

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.marmatsan.dependencies.tree.model.Artifact
import com.marmatsan.dependencies.tree.model.ArtifactsBundle
import com.marmatsan.dependencies.tree.model.DependencyNode
import com.marmatsan.dependencies.tree.model.LibraryEntry
import com.marmatsan.dependencies.tree.node.Node
import org.junit.jupiter.api.Test

internal class LibraryScopeTest {

    @Test
    fun `artifact adds a single entry with version to the created library`() {
        // GIVEN
        val root = Node(DependencyNode.Library("androidx"))
        val scope = LibraryScope(root)

        // WHEN
        scope.library("activity") {
            artifact("activity-compose", version = "1.9.1")
        }

        // THEN
        assertThat(root.children).isEqualTo(
            mutableListOf(
                Node(
                    DependencyNode.Library(
                        libraryGroup = "activity",
                        entries = listOf(
                            LibraryEntry.Single(
                                Artifact("activity-compose", "1.9.1")
                            )
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `artifact adds multiple entries and keeps versionless artifacts and declaration order`() {
        // GIVEN
        val root = Node(DependencyNode.Library("androidx"))
        val scope = LibraryScope(root)

        // WHEN
        scope.library("activity") {
            artifact("activity-compose", version = "1.9.1")
            artifact("activity-ktx")
        }

        // THEN
        assertThat(root.children).isEqualTo(
            mutableListOf(
                Node(
                    DependencyNode.Library(
                        libraryGroup = "activity",
                        entries = listOf(
                            LibraryEntry.Single(
                                Artifact("activity-compose", "1.9.1")
                            ),
                            LibraryEntry.Single(
                                Artifact("activity-ktx")
                            )
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `artifactsBundle adds a bundle entry with alias and versionless artifacts`() {
        // GIVEN
        val root = Node(DependencyNode.Library("androidx"))
        val scope = LibraryScope(root)

        // WHEN
        scope.library("compose") {
            artifactsBundle(
                "ui",
                "ui-graphics",
                "ui-tooling",
                alias = "composeBundle"
            )
        }

        // THEN
        assertThat(root.children).isEqualTo(
            mutableListOf(
                Node(
                    DependencyNode.Library(
                        libraryGroup = "compose",
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
            )
        )
    }

    @Test
    fun `artifactsBundle propagates version to bundle and artifacts`() {
        // GIVEN
        val root = Node(DependencyNode.Library("androidx"))
        val scope = LibraryScope(root)

        // WHEN
        scope.library("compose") {
            artifactsBundle(
                "ui",
                "ui-graphics",
                alias = "composeBundle",
                version = "1.7.0"
            )
        }

        // THEN
        assertThat(root.children).isEqualTo(
            mutableListOf(
                Node(
                    DependencyNode.Library(
                        libraryGroup = "compose",
                        entries = listOf(
                            LibraryEntry.Bundle(
                                ArtifactsBundle(
                                    alias = "composeBundle",
                                    artifacts = listOf(
                                        Artifact("ui", "1.7.0"),
                                        Artifact("ui-graphics", "1.7.0")
                                    ),
                                    version = "1.7.0"
                                )
                            )
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `artifactsBundle can be declared with artifact and keeps entry order`() {
        // GIVEN
        val root = Node(DependencyNode.Library("androidx"))
        val scope = LibraryScope(root)

        // WHEN
        scope.library("compose") {
            artifact("compose-bom", version = "2025.06.01")
            artifactsBundle(
                "ui",
                "ui-graphics",
                alias = "composeBundle"
            )
        }

        // THEN
        assertThat(root.children).isEqualTo(
            mutableListOf(
                Node(
                    DependencyNode.Library(
                        libraryGroup = "compose",
                        entries = listOf(
                            LibraryEntry.Single(
                                Artifact("compose-bom", "2025.06.01")
                            ),
                            LibraryEntry.Bundle(
                                ArtifactsBundle(
                                    alias = "composeBundle",
                                    artifacts = listOf(
                                        Artifact("ui"),
                                        Artifact("ui-graphics")
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `library adds a child library node without entries when content is null`() {
        // GIVEN
        val root = Node(DependencyNode.Library("androidx"))
        val scope = LibraryScope(root)

        // WHEN
        scope.library("compose")

        // THEN
        assertThat(root.children).isEqualTo(
            mutableListOf(
                Node(DependencyNode.Library("compose"))
            )
        )
    }

    @Test
    fun `library supports nested groups and preserves parent entries and children`() {
        // GIVEN
        val root = Node(DependencyNode.Library("androidx"))
        val scope = LibraryScope(root)

        // WHEN
        scope.library("compose") {
            artifact("compose-bom", version = "2025.06.01")
            library("ui") {
                artifact("ui")
            }
        }

        // THEN
        assertThat(root.children).isEqualTo(
            mutableListOf(
                Node(
                    DependencyNode.Library(
                        libraryGroup = "compose",
                        entries = listOf(
                            LibraryEntry.Single(
                                Artifact("compose-bom", "2025.06.01")
                            )
                        )
                    ),
                    children = mutableListOf(
                        Node(
                            DependencyNode.Library(
                                libraryGroup = "ui",
                                entries = listOf(
                                    LibraryEntry.Single(
                                        Artifact("ui")
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `library keeps entries isolated between siblings and preserves sibling order`() {
        // GIVEN
        val root = Node(DependencyNode.Library("androidx"))
        val scope = LibraryScope(root)

        // WHEN
        scope.library("activity") {
            artifact("activity-compose", version = "1.9.1")
        }
        scope.library("compose") {
            artifact("compose-bom", version = "2025.06.01")
        }

        // THEN
        assertThat(root.children).isEqualTo(
            mutableListOf(
                Node(
                    DependencyNode.Library(
                        libraryGroup = "activity",
                        entries = listOf(
                            LibraryEntry.Single(
                                Artifact("activity-compose", "1.9.1")
                            )
                        )
                    )
                ),
                Node(
                    DependencyNode.Library(
                        libraryGroup = "compose",
                        entries = listOf(
                            LibraryEntry.Single(
                                Artifact("compose-bom", "2025.06.01")
                            )
                        )
                    )
                )
            )
        )
    }
}
