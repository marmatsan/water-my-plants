package com.marmatsan.dependencies.tree.mapper

import com.marmatsan.dependencies.tree.model.Artifact
import com.marmatsan.dependencies.tree.model.Dependency
import com.marmatsan.dependencies.tree.model.DependencyNode
import com.marmatsan.dependencies.tree.model.LibraryEntry
import com.marmatsan.unitTest.dsl.given
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class MapperTest :
    FunSpec(
        {
            test("maps NodeData_Library to Dependency_Library when entries is not null") {
                given {
                    DependencyNode.Library(
                        libraryGroup = "androidx.activity",
                        entries =
                            listOf(
                                LibraryEntry.Single(
                                    artifact =
                                        Artifact(
                                            artifact = "activity-compose",
                                            version = "1.9.1",
                                        ),
                                ),
                            ),
                    )
                }.whenever { node ->
                    node.toDependencyLibrary(
                        libraryGroup = "androidx.activity",
                    )
                }.then { dependency ->
                    dependency shouldBe
                        Dependency.Library(
                            libraryGroup = "androidx.activity",
                            entries =
                                listOf(
                                    LibraryEntry.Single(
                                        artifact =
                                            Artifact(
                                                artifact = "activity-compose",
                                                version = "1.9.1",
                                            ),
                                    ),
                                ),
                        )
                }
            }

            test("maps NodeData_Plugin to Dependency_Plugin when version is not null") {
                given {
                    DependencyNode.Plugin(
                        pluginId = "com.android.application",
                        version = "8.10.1",
                    )
                }.whenever { node ->
                    node.toDependencyPlugin(
                        pluginId = "com.android.application",
                    )
                }.then { dependency ->
                    dependency shouldBe
                        Dependency.Plugin(
                            pluginId = "com.android.application",
                            version = "8.10.1",
                        )
                }
            }
        },
    )
