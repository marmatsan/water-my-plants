package com.marmatsan.figmaDesignSync.plugin.checker.catalog

import com.marmatsan.figmaDesignSync.domain.model.catalog.CatalogVersion
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogEntry
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogEntry.ConventionPluginConfigurationUsage
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogEntry.ConventionPluginUsage
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogNode
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaDesignSync.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaDesignSync.domain.model.catalog.PluginCatalogTree
import com.marmatsan.figmaDesignSync.domain.port.catalog.ProjectCatalogTreeSource
import com.marmatsan.figmaDesignSync.domain.port.catalog.ProjectCatalogTreesPort
import com.marmatsan.figmaDesignSync.plugin.generator.FigmaDesignModelIncludedBuildSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files

internal class CatalogUsageCheckerTest : FunSpec({

    test("check reports unused entries from dependency DSL and included-build catalogs") {
        // GIVEN
        val rootDir = Files.createTempDirectory("catalog-usage-check").toFile()
        val checker = CatalogUsageChecker(
            projectCatalogTreesPort = FakeProjectCatalogTreesPort()
        )

        // WHEN
        val result = checker.check(
            CatalogUsageCheckRequest(
                projectRootDirectory = rootDir,
                includedBuilds = listOf(
                    FigmaDesignModelIncludedBuildSource(
                        modelName = "gradlePlugins",
                        settingsFile = rootDir.resolve("repo/gradle-plugins/settings.gradle.kts"),
                        rootDirectory = rootDir.resolve("repo/gradle-plugins"),
                        modulePathPrefix = ":gradle-plugins",
                        publishesCatalogs = true,
                        publishesConventionPlugins = true
                    )
                )
            )
        )

        // THEN
        result.unusedEntries shouldBe listOf(
            UnusedCatalogEntry(
                catalogName = "gradlePlugins.libraries",
                entry = "io.ktor:ktor-client-core"
            ),
            UnusedCatalogEntry(
                catalogName = "gradlePlugins.plugins",
                entry = "org.jetbrains.dokka"
            ),
            UnusedCatalogEntry(
                catalogName = "waterMyPlants.libraries",
                entry = "androidx.datastore:datastore"
            ),
            UnusedCatalogEntry(
                catalogName = "waterMyPlants.plugins",
                entry = "com.google.protobuf"
            )
        )
    }
})

private class FakeProjectCatalogTreesPort : ProjectCatalogTreesPort {
    override fun readLibraryTree(source: ProjectCatalogTreeSource): LibraryCatalogTree =
        when (source) {
            is ProjectCatalogTreeSource.DependenciesDslVersionAliases -> LibraryCatalogTree(
                roots = listOf(
                    LibraryCatalogNode(
                        group = "androidx",
                        children = listOf(
                            LibraryCatalogNode(
                                group = "datastore",
                                entries = listOf(
                                    LibraryCatalogEntry.Artifact(
                                        artifact = "datastore",
                                        version = CatalogVersion("datastoreVersion")
                                    )
                                )
                            )
                        )
                    ),
                    LibraryCatalogNode(
                        group = "com",
                        children = listOf(
                            LibraryCatalogNode(
                                group = "google",
                                children = listOf(
                                    LibraryCatalogNode(
                                        group = "protobuf",
                                        entries = listOf(
                                            LibraryCatalogEntry.Artifact(
                                                artifact = "protobuf-kotlin",
                                                version = CatalogVersion("protobufLibraryVersion"),
                                                providedByConventionPlugins = listOf(
                                                    ConventionPluginUsage(
                                                        pluginId = "com.marmatsan.protobuf",
                                                        pluginModule = ":gradle-plugins:protobuf"
                                                    )
                                                )
                                            ),
                                            LibraryCatalogEntry.Artifact(
                                                artifact = "protoc",
                                                version = CatalogVersion("protobufLibraryVersion"),
                                                configuredByConventionPlugins = listOf(
                                                    ConventionPluginConfigurationUsage(
                                                        pluginId = "com.marmatsan.protobuf",
                                                        pluginModule = ":gradle-plugins:protobuf",
                                                        target = "protobuf.protoc.artifact"
                                                    )
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )

            is ProjectCatalogTreeSource.IncludedBuildSettings -> LibraryCatalogTree(
                roots = listOf(
                    LibraryCatalogNode(
                        group = "io",
                        children = listOf(
                            LibraryCatalogNode(
                                group = "ktor",
                                entries = listOf(
                                    LibraryCatalogEntry.Artifact(
                                        artifact = "ktor-client-core",
                                        version = CatalogVersion(null)
                                    )
                                )
                            )
                        )
                    )
                )
            )

            is ProjectCatalogTreeSource.CustomGradleConventionPlugins,
            is ProjectCatalogTreeSource.CustomGradlePlugins ->
                error("Custom Gradle plugin inventories are not dependency catalogs")
        }

    override fun readPluginTree(source: ProjectCatalogTreeSource): PluginCatalogTree =
        when (source) {
            is ProjectCatalogTreeSource.DependenciesDslVersionAliases -> PluginCatalogTree(
                roots = listOf(
                    PluginCatalogNode(
                        id = "com",
                        children = listOf(
                            PluginCatalogNode(
                                id = "google",
                                children = listOf(
                                    PluginCatalogNode(
                                        id = "protobuf",
                                        version = CatalogVersion("protobufPluginVersion")
                                    )
                                )
                            )
                        )
                    ),
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
                                                id = "plugin",
                                                children = listOf(
                                                    PluginCatalogNode(
                                                        id = "compose",
                                                        version = CatalogVersion("kotlinVersion"),
                                                        providedByConventionPlugins = listOf(
                                                            PluginCatalogNode.ConventionPluginUsage(
                                                                pluginId = "com.marmatsan.compose",
                                                                pluginModule = ":gradle-plugins:compose"
                                                            )
                                                        )
                                                    )
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )

            is ProjectCatalogTreeSource.IncludedBuildSettings -> PluginCatalogTree(
                roots = listOf(
                    PluginCatalogNode(
                        id = "org",
                        children = listOf(
                            PluginCatalogNode(
                                id = "jetbrains",
                                children = listOf(
                                    PluginCatalogNode(
                                        id = "dokka",
                                        version = CatalogVersion("dokkaVersion")
                                    )
                                )
                            )
                        )
                    )
                )
            )

            is ProjectCatalogTreeSource.CustomGradleConventionPlugins,
            is ProjectCatalogTreeSource.CustomGradlePlugins ->
                error("Custom Gradle plugin inventories are not dependency catalogs")
        }
}
