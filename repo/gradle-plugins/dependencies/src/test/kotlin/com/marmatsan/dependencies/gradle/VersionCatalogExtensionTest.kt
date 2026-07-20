package com.marmatsan.dependencies.gradle

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
                // GIVEN
                val alias = "androidx.compose.bom"
                val dependencyNotation = "androidx.compose:compose-bom:2025.06.01"
                val dependency = mockk<MinimalExternalModuleDependency>()
                val provider = mockk<Provider<MinimalExternalModuleDependency>>()
                val versionCatalog = mockk<VersionCatalog>()

                every { dependency.toString() } returns dependencyNotation
                every { provider.get() } returns dependency
                every { versionCatalog.findLibrary(alias) } returns
                    Optional.of(
                        provider,
                    )

                // WHEN
                val actualNotation = versionCatalog.requireDependencyNotation(alias)

                // THEN
                actualNotation shouldBe dependencyNotation
            }

            test("requireBundle returns provider for an existing bundle alias") {
                // GIVEN
                val alias = "composeBundle"
                val provider = mockk<Provider<ExternalModuleDependencyBundle>>()
                val versionCatalog = mockk<VersionCatalog>()

                every {
                    versionCatalog.findBundle(
                        alias,
                    )
                } returns
                    Optional.of(
                        provider,
                    )

                // WHEN
                val actualProvider =
                    versionCatalog.requireBundle(
                        alias = alias,
                    )

                // THEN
                actualProvider shouldBeSameInstanceAs provider
            }

            test("requireDependencyNotation returns notation for an existing library group and artifact") {
                // GIVEN
                val dependencyNotation = "androidx.compose:compose-bom:2025.06.01"
                val dependency = mockk<MinimalExternalModuleDependency>()
                val provider = mockk<Provider<MinimalExternalModuleDependency>>()
                val versionCatalog = mockk<VersionCatalog>()

                every { dependency.toString() } returns dependencyNotation
                every { provider.get() } returns dependency
                every { versionCatalog.findLibrary("androidx.compose.bom") } returns
                    Optional.of(
                        provider,
                    )

                // WHEN
                val actualNotation =
                    versionCatalog.requireDependencyNotation(
                        libraryGroup = "androidx.compose",
                        artifact = "compose-bom",
                    )

                // THEN
                actualNotation shouldBe dependencyNotation
            }

            test("requireDependencyNotation uses the same multi segment alias rule as catalog registration") {
                // GIVEN
                val dependencyNotation = "org.junit.jupiter:junit-jupiter-api"
                val dependency = mockk<MinimalExternalModuleDependency>()
                val provider = mockk<Provider<MinimalExternalModuleDependency>>()
                val versionCatalog = mockk<VersionCatalog>()

                every { dependency.toString() } returns dependencyNotation
                every { provider.get() } returns dependency
                every { versionCatalog.findLibrary("org.junit.jupiter.api") } returns
                    Optional.of(
                        provider,
                    )

                // WHEN
                val actualNotation =
                    versionCatalog.requireDependencyNotation(
                        libraryGroup = "org.junit.jupiter",
                        artifact = "junit-jupiter-api",
                    )

                // THEN
                actualNotation shouldBe dependencyNotation
            }

            test("requireDependencyNotation throws a catalog-specific message when alias does not exist") {
                // GIVEN
                val alias = "androidx.compose.compose.bom"
                val versionCatalog = mockk<VersionCatalog>()

                every { versionCatalog.name } returns "libs"
                every { versionCatalog.findLibrary(alias) } returns Optional.empty()

                // WHEN / THEN
                val exception =
                    shouldThrow<NoSuchElementException> {
                        versionCatalog.requireDependencyNotation(alias)
                    }
                exception.message shouldContain
                    "Library alias 'androidx.compose.compose.bom' not found in version catalog named libs"
            }

            test("requireBundle throws a catalog-specific message when alias does not exist") {
                // GIVEN
                val alias = "missingBundle"
                val versionCatalog = mockk<VersionCatalog>()

                every { versionCatalog.name } returns "libs"
                every {
                    versionCatalog.findBundle(
                        alias,
                    )
                } returns Optional.empty()

                // WHEN / THEN
                val exception =
                    shouldThrow<NoSuchElementException> {
                        versionCatalog.requireBundle(
                            alias = alias,
                        )
                    }
                exception.message shouldContain "Bundle alias 'missingBundle' not found in version catalog named libs"
            }
        },
    )
