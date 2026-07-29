package com.marmatsan.dependencies.catalog.mapping

import com.marmatsan.dependencies.catalog.api.DependencyCatalog
import com.marmatsan.dependencies.catalog.api.LibraryCatalogEntry
import com.marmatsan.dependencies.catalog.api.LibraryCatalogNode
import com.marmatsan.dependencies.catalog.api.PluginCatalogNode
import com.marmatsan.dependencies.catalog.dsl.dependencyCatalogTrees
import com.marmatsan.dependencies.catalog.version.DependencyVersionAliasResolver
import com.marmatsan.unitTest.dsl.given
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class DependencyCatalogApiMappingTest :
    FunSpec(
        {
            test("tree mapping preserves hierarchy entries versions and declaration order") {
                given {
                    dependencyCatalogTrees(
                        versionResolver = DependencyVersionAliasResolver
                    ) {
                        libraries {
                            root("com") {
                                library("example") {
                                    artifact(
                                        artifact = "example-core",
                                        version = version("exampleLibraryVersion")
                                    )
                                    artifactsBundle(
                                        "example-api",
                                        "example-runtime",
                                        alias = "exampleBundle",
                                        version = version("exampleLibraryVersion")
                                    )
                                }
                            }
                        }
                        plugins {
                            root("org") {
                                plugin(
                                    id = "example.quality",
                                    version = version("examplePluginVersion")
                                )
                            }
                        }
                    }
                }.whenever { trees ->
                    trees.toDependencyCatalog()
                }.then { catalog ->
                    catalog shouldBe expectedCatalog
                }
            }
        }
    )

private val expectedCatalog =
    DependencyCatalog(
        libraries =
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
                                            name = "example-core",
                                            version = "exampleLibraryVersion"
                                        ),
                                        LibraryCatalogEntry.Bundle(
                                            alias = "exampleBundle",
                                            artifacts =
                                                listOf(
                                                    "example-api",
                                                    "example-runtime"
                                                ),
                                            version = "exampleLibraryVersion"
                                        )
                                    )
                            )
                        )
                )
            ),
        plugins =
            listOf(
                PluginCatalogNode(
                    id = "org",
                    children =
                        listOf(
                            PluginCatalogNode(
                                id = "example",
                                children =
                                    listOf(
                                        PluginCatalogNode(
                                            id = "quality",
                                            version = "examplePluginVersion"
                                        )
                                    )
                            )
                        )
                )
            )
    )
