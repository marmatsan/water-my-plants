package com.marmatsan.figmaDesignSync.data.dependencies.catalog

import com.marmatsan.dependencies.tree.dsl.library.libraryTree
import com.marmatsan.dependencies.tree.dsl.plugin.pluginTree
import com.marmatsan.figmaDesignSync.data.gradle.catalog.GradleCatalogUsageReader
import com.marmatsan.figmaDesignSync.domain.model.catalog.CatalogVersion
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogEntry
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogNode
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaDesignSync.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaDesignSync.domain.model.catalog.PluginCatalogTree
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files

internal class DependenciesCatalogTreesReaderTest : FunSpec({

    test("readLibraryTree maps dependency library trees to catalog library trees") {
        // GIVEN
        val dependencyTree = libraryTree("androidx") {
            library("compose") {
                artifact(
                    artifact = "compose-bom",
                    version = "2026.05.01"
                )
                library("ui") {
                    artifactsBundle(
                        "ui",
                        "ui-tooling",
                        alias = "composeBundle"
                    )
                }
            }
        }

        // WHEN
        val actualTree = dependenciesCatalogTreesReader().readLibraryTree(listOf(dependencyTree))

        // THEN
        actualTree shouldBe LibraryCatalogTree(
            roots = listOf(
                LibraryCatalogNode(
                    group = "androidx",
                    children = listOf(
                        LibraryCatalogNode(
                            group = "compose",
                            entries = listOf(
                                LibraryCatalogEntry.Artifact(
                                    artifact = "compose-bom",
                                    version = CatalogVersion("2026.05.01")
                                )
                            ),
                            children = listOf(
                                LibraryCatalogNode(
                                    group = "ui",
                                    entries = listOf(
                                        LibraryCatalogEntry.ArtifactsBundle(
                                            alias = "composeBundle",
                                            artifacts = listOf(
                                                "ui",
                                                "ui-tooling"
                                            ),
                                            version = CatalogVersion(null)
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    }

    test("readPluginTree maps dependency plugin trees to catalog plugin trees") {
        // GIVEN
        val dependencyTree = pluginTree("org") {
            plugin("jetbrains") {
                plugin("kotlin") {
                    plugin(
                        id = "android",
                        version = "2.4.0"
                    )
                }
            }
        }

        // WHEN
        val actualTree = dependenciesCatalogTreesReader().readPluginTree(listOf(dependencyTree))

        // THEN
        actualTree shouldBe PluginCatalogTree(
            roots = listOf(
                PluginCatalogNode(
                    id = "org",
                    children = listOf(
                        PluginCatalogNode(
                            id = "jetbrains",
                            children = listOf(
                                PluginCatalogNode(
                                    id = "kotlin",
                                    children = listOf(
                                        PluginCatalogNode(
                                            id = "android",
                                            version = CatalogVersion("2.4.0")
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    }

    test("readLibraryTreeWithVersionAliases maps versions as property aliases") {
        // WHEN
        val actualTree = dependenciesCatalogTreesReader().readLibraryTreeWithVersionAliases(rootDir = java.io.File("."))

        // THEN
        val activity = actualTree.roots
            .first { node -> node.group == "androidx" }
            .children
            .first { node -> node.group == "activity" }
            .entries
            .single() as LibraryCatalogEntry.Artifact

        activity.version shouldBe CatalogVersion("activityComposeVersion")
    }

    test("readLibraryTreeWithVersionAliases scopes required modules to the main build catalog") {
        // GIVEN
        val rootDir = Files.createTempDirectory("water-my-plants-library-usages").toFile()
        rootDir.writeBuildFile(
            path = "app",
            content = """
            dependencies {
                implementation(libs.androidx.core.ktx)
                implementation(platform(libs.androidx.compose.bom))
                implementation(libs.bundles.composeBundle)
            }
            """.trimIndent()
        )
        rootDir.writeSettingsFile(
            path = "repo/gradle-plugins",
            content = """
            rootProject.name = "gradle-plugins"
            include(":unit-test")
            """.trimIndent()
        )
        rootDir.writeBuildFile(
            path = "repo/gradle-plugins/unit-test",
            content = """
            dependencies {
                testImplementation(libs.io.mockk)
            }
            """.trimIndent()
        )

        // WHEN
        val actualTree = dependenciesCatalogTreesReader().readLibraryTreeWithVersionAliases(rootDir = rootDir)

        // THEN
        actualTree.findArtifact("androidx.core", "core-ktx").requiredByModules shouldBe listOf(":app")
        actualTree.findArtifact("androidx.compose", "compose-bom").requiredByModules shouldBe listOf(":app")
        actualTree.findBundle("androidx.compose.ui", "composeBundle").requiredByModules shouldBe listOf(":app")
        actualTree.findArtifact("io.mockk", "mockk").requiredByModules shouldBe emptyList()
    }

    test("readPluginTreeWithVersionAliases scopes applied modules to the main build catalog") {
        // GIVEN
        val rootDir = Files.createTempDirectory("water-my-plants-plugin-usages").toFile()
        rootDir.writeBuildFile(
            path = "app",
            content = """
            plugins {
                alias(plugins.plugins.com.android.application)
            }
            """.trimIndent()
        )
        rootDir.writeBuildFile(
            path = "core/ui",
            content = """
            plugins {
                alias(plugins.plugins.com.android.library)
            }
            """.trimIndent()
        )
        rootDir.writeSettingsFile(
            path = "repo/gradle-plugins",
            content = """
            rootProject.name = "gradle-plugins"
            include(":dokka-documentation")
            """.trimIndent()
        )
        rootDir.writeBuildFile(
            path = "repo/gradle-plugins/dokka-documentation",
            content = """
            plugins {
                alias(plugins.plugins.org.jetbrains.dokka)
            }
            """.trimIndent()
        )

        // WHEN
        val actualTree = dependenciesCatalogTreesReader().readPluginTreeWithVersionAliases(rootDir = rootDir)

        // THEN
        actualTree.findPlugin("com.android.application").appliedToModules shouldBe listOf(":app")
        actualTree.findPlugin("com.android.library").appliedToModules shouldBe listOf(":core:ui")
        actualTree.findPlugin("org.jetbrains.dokka").appliedToModules shouldBe emptyList()
    }
})

private fun dependenciesCatalogTreesReader() =
    DependenciesCatalogTreesReader(
        gradleCatalogUsageReader = GradleCatalogUsageReader()
    )

private fun LibraryCatalogTree.findArtifact(
    groupPath: String,
    artifact: String
): LibraryCatalogEntry.Artifact =
    findLibraryNode(groupPath)
        .entries
        .filterIsInstance<LibraryCatalogEntry.Artifact>()
        .first { entry -> entry.artifact == artifact }

private fun LibraryCatalogTree.findBundle(
    groupPath: String,
    alias: String
): LibraryCatalogEntry.ArtifactsBundle =
    findLibraryNode(groupPath)
        .entries
        .filterIsInstance<LibraryCatalogEntry.ArtifactsBundle>()
        .first { entry -> entry.alias == alias }

private fun LibraryCatalogTree.findLibraryNode(groupPath: String): LibraryCatalogNode {
    val segments = groupPath.split(".")

    return segments.foldIndexed(null as LibraryCatalogNode?) { index, node, segment ->
        val candidates = if (index == 0) roots else node?.children.orEmpty()
        candidates.first { candidate -> candidate.group == segment }
    } ?: error("Library group '$groupPath' was not found")
}

private fun PluginCatalogTree.findPlugin(pluginId: String): PluginCatalogNode {
    val segments = pluginId.split(".")

    return segments.foldIndexed(null as PluginCatalogNode?) { index, node, segment ->
        val candidates = if (index == 0) roots else node?.children.orEmpty()
        candidates.first { candidate -> candidate.id == segment }
    } ?: error("Plugin '$pluginId' was not found")
}

private fun File.writeSettingsFile(
    path: String,
    content: String
) {
    val directory = resolve(path)
    directory.mkdirs()
    directory.resolve("settings.gradle.kts").writeText(content)
}

private fun File.writeBuildFile(
    path: String,
    content: String
) {
    val directory = resolve(path)
    directory.mkdirs()
    directory.resolve("build.gradle.kts").writeText(content)
}
