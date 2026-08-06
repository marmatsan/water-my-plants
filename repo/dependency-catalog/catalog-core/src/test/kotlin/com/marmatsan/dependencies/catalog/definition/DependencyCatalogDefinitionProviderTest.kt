package com.marmatsan.dependencies.catalog.definition

import com.marmatsan.dependencies.catalog.api.LibraryCatalogEntry
import com.marmatsan.unitTest.dsl.given
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe

internal class DependencyCatalogDefinitionProviderTest :
    FunSpec(
        {
            test("one definition provides resolved and version-aliased catalog views") {
                given {
                    val rootDirectory =
                        tempdir(
                            prefix = "dependency-catalog-definition"
                        ).apply {
                            resolve("versions.properties").writeText(
                                """
                                exampleLibraryVersion=1.2.3
                                examplePluginVersion=2.0.0
                                """.trimIndent()
                            )
                        }
                    val provider =
                        DependencyCatalogDefinitionProvider(
                            definition = exampleCatalogDefinition
                        )
                    DefinitionScenario(
                        rootDirectory = rootDirectory,
                        provider = provider
                    )
                }.whenever { scenario ->
                    listOf(
                        scenario.provider.resolved(scenario.rootDirectory),
                        scenario.provider.withVersionAliases()
                    ).map { catalog ->
                        CatalogVersions(
                            library =
                                (
                                    catalog.libraries
                                        .single()
                                        .children
                                        .single()
                                        .entries
                                        .single() as LibraryCatalogEntry.Artifact
                                ).version,
                            plugin =
                                catalog.plugins
                                    .single()
                                    .children
                                    .single()
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
        }
    )

private val exampleCatalogDefinition =
    dependencyCatalogDefinition {
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
            root("org") {
                plugin(
                    id = "example",
                    version = version("examplePluginVersion")
                )
            }
        }
    }

private data class DefinitionScenario(
    val rootDirectory: java.io.File,
    val provider: DependencyCatalogDefinitionProvider
)

private data class CatalogVersions(
    val library: String?,
    val plugin: String?
)
