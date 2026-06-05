package com.marmatsan.dependencies.gradle

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSameInstanceAs
import assertk.assertions.messageContains
import io.mockk.every
import io.mockk.mockk
import org.gradle.api.artifacts.ExternalModuleDependencyBundle
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.provider.Provider
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Test
import java.util.Optional

internal class VersionCatalogExtensionTest {

    @Test
    fun `requireDependencyNotation returns notation for an existing library alias`() {
        // GIVEN
        val alias = "androidx.compose.bom"
        val dependencyNotation = "androidx.compose:compose-bom:2025.06.01"
        val dependency = mockk<MinimalExternalModuleDependency>()
        val provider = mockk<Provider<MinimalExternalModuleDependency>>()
        val versionCatalog = mockk<VersionCatalog>()

        every { dependency.toString() } returns dependencyNotation
        every { provider.get() } returns dependency
        every { versionCatalog.findLibrary(alias) } returns Optional.of(provider)

        // WHEN
        val actualNotation = versionCatalog.requireDependencyNotation(alias)

        // THEN
        assertThat(actualNotation).isEqualTo(dependencyNotation)
    }

    @Test
    fun `requireBundle returns provider for an existing bundle alias`() {
        // GIVEN
        val alias = "composeBundle"
        val provider = mockk<Provider<ExternalModuleDependencyBundle>>()
        val versionCatalog = mockk<VersionCatalog>()

        every { versionCatalog.findBundle(alias) } returns Optional.of(provider)

        // WHEN
        val actualProvider = versionCatalog.requireBundle(alias)

        // THEN
        assertThat(actualProvider).isSameInstanceAs(provider)
    }

    @Test
    fun `requireDependencyNotation returns notation for an existing library group and artifact`() {
        // GIVEN
        val dependencyNotation = "androidx.compose:compose-bom:2025.06.01"
        val dependency = mockk<MinimalExternalModuleDependency>()
        val provider = mockk<Provider<MinimalExternalModuleDependency>>()
        val versionCatalog = mockk<VersionCatalog>()

        every { dependency.toString() } returns dependencyNotation
        every { provider.get() } returns dependency
        every { versionCatalog.findLibrary("androidx.compose.bom") } returns Optional.of(provider)

        // WHEN
        val actualNotation = versionCatalog.requireDependencyNotation(
            libraryGroup = "androidx.compose",
            artifact = "compose-bom"
        )

        // THEN
        assertThat(actualNotation).isEqualTo(dependencyNotation)
    }

    @Test
    fun `requireDependencyNotation uses the same multi segment alias rule as catalog registration`() {
        // GIVEN
        val dependencyNotation = "org.junit.jupiter:junit-jupiter-api"
        val dependency = mockk<MinimalExternalModuleDependency>()
        val provider = mockk<Provider<MinimalExternalModuleDependency>>()
        val versionCatalog = mockk<VersionCatalog>()

        every { dependency.toString() } returns dependencyNotation
        every { provider.get() } returns dependency
        every { versionCatalog.findLibrary("org.junit.jupiter.api") } returns Optional.of(provider)

        // WHEN
        val actualNotation = versionCatalog.requireDependencyNotation(
            libraryGroup = "org.junit.jupiter",
            artifact = "junit-jupiter-api"
        )

        // THEN
        assertThat(actualNotation).isEqualTo(dependencyNotation)
    }

    @Test
    fun `requireDependencyNotation throws a catalog-specific message when alias does not exist`() {
        // GIVEN
        val alias = "androidx.compose.compose.bom"
        val versionCatalog = mockk<VersionCatalog>()

        every { versionCatalog.name } returns "libs"
        every { versionCatalog.findLibrary(alias) } returns Optional.empty()

        // WHEN / THEN
        val exception = assertThrows<NoSuchElementException> {
            versionCatalog.requireDependencyNotation(alias)
        }
        assertThat(exception).messageContains(
            "Library alias 'androidx.compose.compose.bom' not found in version catalog named libs"
        )
    }

    @Test
    fun `requireBundle throws a catalog-specific message when alias does not exist`() {
        // GIVEN
        val alias = "missingBundle"
        val versionCatalog = mockk<VersionCatalog>()

        every { versionCatalog.name } returns "libs"
        every { versionCatalog.findBundle(alias) } returns Optional.empty()

        // WHEN / THEN
        val exception = assertThrows<NoSuchElementException> {
            versionCatalog.requireBundle(alias)
        }
        assertThat(exception).messageContains(
            "Bundle alias 'missingBundle' not found in version catalog named libs"
        )
    }
}
