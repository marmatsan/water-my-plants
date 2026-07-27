package com.marmatsan.dependencies.gradle

import com.marmatsan.dependencies.catalog.api.LibraryCatalogEntry
import com.marmatsan.unitTest.dsl.given
import io.kotest.core.spec.style.FunSpec
import io.mockk.mockk
import io.mockk.verify
import org.gradle.api.initialization.dsl.VersionCatalogBuilder

internal class VersionCatalogBuilderExtensionTest :
    FunSpec(
        {
            test("registerLibraries registers a library alias without duplicating artifact prefix shared with group") {
                given {
                    RegistrationFixture(
                        builder =
                            mockk(
                                relaxed = true,
                            ),
                        values =
                            listOf(
                                ResolvedLibrary(
                                    group = "androidx.activity",
                                    entries =
                                        listOf(
                                            LibraryCatalogEntry.Artifact(
                                                name = "activity-compose",
                                                version = "1.9.2",
                                            ),
                                        ),
                                ),
                            ),
                    )
                }.whenever { fixture ->
                    fixture.builder.registerLibraries(
                        libraries = fixture.values,
                    )
                }.then { fixture, _ ->
                    verify {
                        fixture.builder
                            .library(
                                "androidx.activity.compose",
                                "androidx.activity",
                                "activity-compose",
                            ).version(
                                "1.9.2",
                            )
                    }
                }
            }

            test("registerLibraries registers a BOM alias without duplicating artifact prefix shared with group") {
                given {
                    RegistrationFixture(
                        builder =
                            mockk(
                                relaxed = true,
                            ),
                        values =
                            listOf(
                                ResolvedLibrary(
                                    group = "androidx.compose",
                                    entries =
                                        listOf(
                                            LibraryCatalogEntry.Artifact(
                                                name = "compose-bom",
                                                version = "2025.06.01",
                                            ),
                                        ),
                                ),
                            ),
                    )
                }.whenever { fixture ->
                    fixture.builder.registerLibraries(
                        libraries = fixture.values,
                    )
                }.then { fixture, _ ->
                    verify {
                        fixture.builder
                            .library(
                                "androidx.compose.bom",
                                "androidx.compose",
                                "compose-bom",
                            ).version(
                                "2025.06.01",
                            )
                    }
                }
            }

            test(
                "registerLibraries registers an artifact alias preserving group when artifact does not share group prefix",
            ) {
                given {
                    RegistrationFixture(
                        builder =
                            mockk(
                                relaxed = true,
                            ),
                        values =
                            listOf(
                                ResolvedLibrary(
                                    group = "androidx.compose.ui",
                                    entries =
                                        listOf(
                                            LibraryCatalogEntry.Artifact(
                                                name = "ui-tooling-preview",
                                                version = null,
                                            ),
                                        ),
                                ),
                            ),
                    )
                }.whenever { fixture ->
                    fixture.builder.registerLibraries(
                        libraries = fixture.values,
                    )
                }.then { fixture, _ ->
                    verify {
                        fixture.builder
                            .library(
                                "androidx.compose.ui.tooling.preview",
                                "androidx.compose.ui",
                                "ui-tooling-preview",
                            ).withoutVersion()
                    }
                }
            }

            test(
                "registerLibraries registers a library alias without duplicating multi segment artifact prefix shared with group",
            ) {
                given {
                    RegistrationFixture(
                        builder =
                            mockk(
                                relaxed = true,
                            ),
                        values =
                            listOf(
                                ResolvedLibrary(
                                    group = "org.junit.jupiter",
                                    entries =
                                        listOf(
                                            LibraryCatalogEntry.Artifact(
                                                name = "junit-jupiter-api",
                                                version = null,
                                            ),
                                        ),
                                ),
                            ),
                    )
                }.whenever { fixture ->
                    fixture.builder.registerLibraries(
                        libraries = fixture.values,
                    )
                }.then { fixture, _ ->
                    verify {
                        fixture.builder
                            .library(
                                "org.junit.jupiter.api",
                                "org.junit.jupiter",
                                "junit-jupiter-api",
                            ).withoutVersion()
                    }
                }
            }

            test("registerLibraries registers bundle aliases using generated library aliases") {
                given {
                    RegistrationFixture(
                        builder =
                            mockk(
                                relaxed = true,
                            ),
                        values =
                            listOf(
                                ResolvedLibrary(
                                    group = "androidx.compose.ui",
                                    entries =
                                        listOf(
                                            LibraryCatalogEntry.Bundle(
                                                alias = "composeUiBundle",
                                                artifacts =
                                                    listOf(
                                                        "ui",
                                                        "ui-graphics",
                                                        "ui-tooling",
                                                    ),
                                                version = null,
                                            ),
                                        ),
                                ),
                            ),
                    )
                }.whenever { fixture ->
                    fixture.builder.registerLibraries(
                        libraries = fixture.values,
                    )
                }.then { fixture, _ ->
                    verify {
                        fixture.builder.bundle(
                            "composeUiBundle",
                            listOf(
                                "androidx.compose.ui",
                                "androidx.compose.ui.graphics",
                                "androidx.compose.ui.tooling",
                            ),
                        )
                    }
                }
            }

            test("registerPlugins registers plugins") {
                given {
                    RegistrationFixture(
                        builder =
                            mockk(
                                relaxed = true,
                            ),
                        values =
                            listOf(
                                ResolvedPlugin(
                                    id = "com.android.application",
                                    version = "8.13.2",
                                ),
                            ),
                    )
                }.whenever { fixture ->
                    fixture.builder.registerPlugins(
                        plugins = fixture.values,
                    )
                }.then { fixture, _ ->
                    verify {
                        fixture.builder
                            .plugin(
                                "com.android.application",
                                "com.android.application",
                            ).version(
                                "8.13.2",
                            )
                    }
                }
            }
        },
    )

private data class RegistrationFixture<Value>(
    val builder: VersionCatalogBuilder,
    val values: List<Value>,
)
