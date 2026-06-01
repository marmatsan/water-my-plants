package com.marmatsan.dependencies.gradle

import com.marmatsan.dependencies.tree.model.Artifact
import com.marmatsan.dependencies.tree.model.ArtifactsBundle
import com.marmatsan.dependencies.tree.model.Dependency
import com.marmatsan.dependencies.tree.model.LibraryEntry
import io.mockk.mockk
import io.mockk.verify
import org.gradle.api.initialization.dsl.VersionCatalogBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class VersionCatalogBuilderExtensionKtTest {

    private lateinit var builder: VersionCatalogBuilder

    @BeforeEach
    fun setup() {
        builder = mockk<VersionCatalogBuilder>(relaxed = true)
    }

    @Test
    fun `registerLibrary successfully registers a single library`() {
        // GIVEN
        val libraries = listOf(
            Dependency.Library(
                libraryGroup = "androidx.activity",
                entries = listOf(
                    LibraryEntry.Single(
                        artifact = Artifact(
                            artifact = "activity-compose",
                            version = "1.9.2"
                        )
                    )
                )
            )
        )

        // WHEN
        builder.registerLibraries(libraries)

        // THEN
        verify {
            builder.library(
                "androidx.activity-compose",
                "androidx.activity",
                "activity-compose"
            ).version("1.9.2")
        }
    }

    @Test
    fun `registerLibraries successfully registers a library bundle managed by a BOM`() {
        // GIVEN
        val libraries = listOf(
            Dependency.Library(
                libraryGroup = "androidx.compose",
                entries = listOf(
                    LibraryEntry.Single(
                        artifact = Artifact(
                            artifact = "compose-bom",
                            version = "2025.06.01"
                        )
                    ),
                    LibraryEntry.Bundle(
                        artifactsBundle = ArtifactsBundle(
                            alias = "composeBundle",
                            artifacts = listOf(
                                Artifact(
                                    artifact = "ui"
                                ),
                                Artifact(
                                    artifact = "ui-graphics"
                                ),
                                Artifact(
                                    artifact = "ui-tooling"
                                ),
                                Artifact(
                                    artifact = "ui-tooling-preview"
                                )
                            )
                        )
                    )
                )
            )
        )

        // WHEN
        builder.registerLibraries(libraries)

        // THEN
        verify {
            builder.library(
                "androidx.compose-bom",
                "androidx.compose",
                "compose-bom"
            ).version("2025.06.01")
            builder.bundle(
                "composeBundle",
                listOf(
                    "androidx.compose.ui",
                    "androidx.compose.ui-graphics",
                    "androidx.compose.ui-tooling",
                    "androidx.compose.ui-tooling-preview"
                )
            )
        }
    }

    @Test
    fun `registerPlugins registers only plugins with versions`() {
        // GIVEN
        val plugins = listOf(
            Dependency.Plugin(
                pluginId = "com.android.application",
                version = "8.13.2"
            ),
            Dependency.Plugin(
                pluginId = "org.jetbrains.kotlin.android",
                version = null
            )
        )

        // WHEN
        builder.registerPlugins(plugins)

        // THEN
        verify {
            builder.plugin(
                "com.android.application",
                "com.android.application"
            ).version("8.13.2")
        }
        verify(exactly = 0) {
            builder.plugin(
                "org.jetbrains.kotlin.android",
                "org.jetbrains.kotlin.android"
            )
        }
    }
}
