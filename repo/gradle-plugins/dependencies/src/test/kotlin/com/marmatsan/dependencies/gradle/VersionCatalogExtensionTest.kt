package com.marmatsan.dependencies.gradle

import com.marmatsan.unitTest.dsl.given
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import org.gradle.api.artifacts.ExternalModuleDependencyBundle
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.provider.Provider
import java.util.Optional

internal class VersionCatalogExtensionTest :
    FunSpec(
        {
            test("requireDependencyNotation returns notation for an existing library alias") {
                given {
                    val dependencyNotation = "androidx.compose:compose-bom:2025.06.01"
                    val dependency = mockk<MinimalExternalModuleDependency>()
                    val provider = mockk<Provider<MinimalExternalModuleDependency>>()
                    val versionCatalog = mockk<VersionCatalog>()

                    every { dependency.toString() } returns dependencyNotation
                    every { provider.get() } returns dependency
                    every { versionCatalog.findLibrary("androidx.compose.bom") } returns
                        Optional.of(
                            provider
                        )

                    dependencyNotation to versionCatalog
                }.whenever { (expectedNotation, versionCatalog) ->
                    versionCatalog.requireDependencyNotation("androidx.compose.bom") to expectedNotation
                }.then { (actualNotation, expectedNotation) ->
                    actualNotation shouldBe expectedNotation
                }
            }

            test("requireBundle returns provider for an existing bundle alias") {
                given {
                    val provider = mockk<Provider<ExternalModuleDependencyBundle>>()
                    val versionCatalog = mockk<VersionCatalog>()

                    every {
                        versionCatalog.findBundle(
                            "composeBundle"
                        )
                    } returns
                        Optional.of(
                            provider
                        )

                    provider to versionCatalog
                }.whenever { (expectedProvider, versionCatalog) ->
                    versionCatalog.requireBundle(
                        alias = "composeBundle"
                    ) to expectedProvider
                }.then { (actualProvider, expectedProvider) ->
                    actualProvider shouldBeSameInstanceAs expectedProvider
                }
            }

            test("requireDependencyNotation returns notation for an existing library group and artifact") {
                given {
                    val dependencyNotation = "androidx.compose:compose-bom:2025.06.01"
                    val dependency = mockk<MinimalExternalModuleDependency>()
                    val provider = mockk<Provider<MinimalExternalModuleDependency>>()
                    val versionCatalog = mockk<VersionCatalog>()

                    every { dependency.toString() } returns dependencyNotation
                    every { provider.get() } returns dependency
                    every { versionCatalog.findLibrary("androidx.compose.bom") } returns
                        Optional.of(
                            provider
                        )

                    dependencyNotation to versionCatalog
                }.whenever { (expectedNotation, versionCatalog) ->
                    versionCatalog.requireDependencyNotation(
                        libraryGroup = "androidx.compose",
                        artifact = "compose-bom"
                    ) to expectedNotation
                }.then { (actualNotation, expectedNotation) ->
                    actualNotation shouldBe expectedNotation
                }
            }

            test("requireDependencyNotation uses the same multi segment alias rule as catalog registration") {
                given {
                    val dependencyNotation = "org.junit.jupiter:junit-jupiter-api"
                    val dependency = mockk<MinimalExternalModuleDependency>()
                    val provider = mockk<Provider<MinimalExternalModuleDependency>>()
                    val versionCatalog = mockk<VersionCatalog>()

                    every { dependency.toString() } returns dependencyNotation
                    every { provider.get() } returns dependency
                    every { versionCatalog.findLibrary("org.junit.jupiter.api") } returns
                        Optional.of(
                            provider
                        )

                    dependencyNotation to versionCatalog
                }.whenever { (expectedNotation, versionCatalog) ->
                    versionCatalog.requireDependencyNotation(
                        libraryGroup = "org.junit.jupiter",
                        artifact = "junit-jupiter-api"
                    ) to expectedNotation
                }.then { (actualNotation, expectedNotation) ->
                    actualNotation shouldBe expectedNotation
                }
            }

            test("requireDependencyNotation throws a catalog-specific message when alias does not exist") {
                given {
                    val versionCatalog = mockk<VersionCatalog>()
                    every { versionCatalog.name } returns "libs"
                    every { versionCatalog.findLibrary("androidx.compose.compose.bom") } returns Optional.empty()
                    versionCatalog
                }.whenever { versionCatalog ->
                    shouldThrow<NoSuchElementException> {
                        versionCatalog.requireDependencyNotation("androidx.compose.compose.bom")
                    }
                }.then { exception ->
                    exception.message shouldContain
                        "Library alias 'androidx.compose.compose.bom' not found in version catalog named libs"
                }
            }

            test("requireBundle throws a catalog-specific message when alias does not exist") {
                given {
                    val versionCatalog = mockk<VersionCatalog>()
                    every { versionCatalog.name } returns "libs"
                    every {
                        versionCatalog.findBundle(
                            "missingBundle"
                        )
                    } returns Optional.empty()
                    versionCatalog
                }.whenever { versionCatalog ->
                    shouldThrow<NoSuchElementException> {
                        versionCatalog.requireBundle(
                            alias = "missingBundle"
                        )
                    }
                }.then { exception ->
                    exception.message shouldContain
                        "Bundle alias 'missingBundle' not found in version catalog named libs"
                }
            }
        }
    )
