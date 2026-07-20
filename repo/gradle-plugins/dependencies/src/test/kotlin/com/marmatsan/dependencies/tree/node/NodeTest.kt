package com.marmatsan.dependencies.tree.node

import com.marmatsan.dependencies.tree.mapper.toDependencyLibrary
import com.marmatsan.dependencies.tree.mapper.toDependencyPlugin
import com.marmatsan.dependencies.tree.model.Artifact
import com.marmatsan.dependencies.tree.model.ArtifactsBundle
import com.marmatsan.dependencies.tree.model.Dependency
import com.marmatsan.dependencies.tree.model.DependencyNode
import com.marmatsan.dependencies.tree.model.LibraryEntry
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class NodeTest : FunSpec(
    {

    test("depthFirstPreOrderTraverse maps a given library tree preserving full paths and pre order") {
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
                libraryNode.toDependencyLibrary(
                    libraryGroup = fullPath
                )
            }
        )

        // THEN
        actualLibraries shouldBe listOf(
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
    }

    test("depthFirstPreOrderTraverse maps a given plugin tree preserving full paths and pre order") {
        // GIVEN
        val com = Node(DependencyNode.Plugin("com"))
        val android = Node(DependencyNode.Plugin("android"))
        val application = Node(
            DependencyNode.Plugin(
                pluginId = "application",
                version = "9.2.1"
            )
        )
        val library = Node(
            DependencyNode.Plugin(
                pluginId = "library",
                version = "9.2.1"
            )
        )
        val google = Node(DependencyNode.Plugin("google"))
        val devtools = Node(DependencyNode.Plugin("devtools"))
        val ksp = Node(
            DependencyNode.Plugin(
                pluginId = "ksp",
                version = "2.3.9"
            )
        )

        com.add(android)
        android.add(application)
        android.add(library)
        com.add(google)
        google.add(devtools)
        devtools.add(ksp)

        // WHEN
        val actualPlugins = com.depthFirstPreOrderTraverse(
            pathSegment = DependencyNode.Plugin::pluginId,
            shouldIncludeNode = { it.version != null },
            mapNode = { pluginNode, fullPath ->
                pluginNode.toDependencyPlugin(
                    pluginId = fullPath
                )
            }
        )

        // THEN
        actualPlugins shouldBe listOf(
            Dependency.Plugin(
                pluginId = "com.android.application",
                version = "9.2.1"
            ),
            Dependency.Plugin(
                pluginId = "com.android.library",
                version = "9.2.1"
            ),
            Dependency.Plugin(
                pluginId = "com.google.devtools.ksp",
                version = "2.3.9"
            )
        )
    }
}
)
