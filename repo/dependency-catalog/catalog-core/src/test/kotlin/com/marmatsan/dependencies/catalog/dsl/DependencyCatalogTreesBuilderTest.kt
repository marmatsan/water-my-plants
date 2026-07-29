package com.marmatsan.dependencies.catalog.dsl

import com.marmatsan.dependencies.catalog.version.DependencyVersionAliasResolver
import com.marmatsan.dependencies.catalog.version.DependencyVersionResolver
import com.marmatsan.dependencies.tree.model.LibraryEntry
import com.marmatsan.unitTest.dsl.given
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class DependencyCatalogTreesBuilderTest :
    FunSpec(
        {
            test("one declaration supports resolved and symbolic version strategies") {
                given {
                    listOf(
                        dependencyCatalog(
                            versionResolver =
                                DependencyVersionResolver { key ->
                                    mapOf(
                                        "exampleLibraryVersion" to "1.2.3",
                                        "examplePluginVersion" to "2.0.0"
                                    ).getValue(key)
                                }
                        ),
                        dependencyCatalog(
                            versionResolver = DependencyVersionAliasResolver
                        )
                    )
                }.whenever { catalogs ->
                    catalogs.map { catalog ->
                        CatalogVersions(
                            library =
                                catalog.libraries
                                    .single()
                                    .children
                                    .single()
                                    .value
                                    .entries
                                    .orEmpty()
                                    .single()
                                    .version(),
                            plugin =
                                catalog.plugins
                                    .single()
                                    .children
                                    .single()
                                    .value
                                    .version
                        )
                    }
                }.then { versions ->
                    versions shouldBe
                        listOf(
                            CatalogVersions(
                                library = "1.2.3",
                                plugin = "2.0.0"
                            ),
                            CatalogVersions(
                                library = "exampleLibraryVersion",
                                plugin = "examplePluginVersion"
                            )
                        )
                }
            }

            test("builder requires at least one root") {
                given {
                    DependencyCatalogTreesBuilder(
                        versionResolver = DependencyVersionAliasResolver
                    )
                }.whenever { builder ->
                    shouldThrow<IllegalArgumentException> {
                        builder.build()
                    }
                }.then { failure ->
                    failure.message shouldBe
                        "Tree dependency catalog must declare at least one library or plugin root"
                }
            }

            test("builder cannot be reused after producing its catalog") {
                given {
                    DependencyCatalogTreesBuilder(
                        versionResolver = DependencyVersionAliasResolver
                    ).apply {
                        libraries {
                            root("com") {}
                        }
                    }
                }.whenever { builder ->
                    builder.build()
                    shouldThrow<IllegalStateException> {
                        builder.plugins {
                            root("org") {}
                        }
                    }
                }.then { failure ->
                    failure.message shouldBe
                        "Dependency catalog trees builder cannot be reused after build()"
                }
            }
        }
    )

private fun dependencyCatalog(
    versionResolver: DependencyVersionResolver
) = dependencyCatalogTrees(
    versionResolver = versionResolver
) {
    libraries {
        root("com") {
            library("example") {
                artifact(
                    artifact = "example-core",
                    version = version("exampleLibraryVersion")
                )
            }
        }
    }
    plugins {
        root("com") {
            plugin(
                id = "example",
                version = version("examplePluginVersion")
            )
        }
    }
}

private fun LibraryEntry.version(): String? =
    when (this) {
        is LibraryEntry.Single -> artifact.version
        is LibraryEntry.Bundle -> artifactsBundle.version
    }

private data class CatalogVersions(
    val library: String?,
    val plugin: String?
)
