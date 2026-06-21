package com.marmatsan.figmaDesignSync.data.gradle.catalog

import com.marmatsan.figmaDesignSync.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaDesignSync.domain.model.catalog.PluginCatalogTree
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files

internal class GradleConventionPluginTreeReaderTest : FunSpec({

    test("readPluginTree detects any Gradle convention plugin build file under build logic") {
        // GIVEN
        val rootDir = Files.createTempDirectory("gradle-convention-plugin-tree").toFile()
        rootDir.writeBuildFile(
            path = "build-logic/analytics",
            content = conventionPluginBuildFile(
                pluginName = "com.marmatsan.analytics",
                implementationClass = "\${pluginName}.plugin.AnalyticsGradleConventionPlugin"
            )
        )
        rootDir.writeBuildFile(
            path = "build-logic/figmaDesignSync/plugin",
            content = conventionPluginBuildFile(
                pluginName = "com.marmatsan.figmaDesignSync",
                implementationClass = "\${pluginName}.plugin.gradle.figmaDesignSyncGradleConventionPlugin"
            )
        )
        rootDir.writeBuildFile(
            path = "build-logic/reporting",
            content = literalIdConventionPluginBuildFile(
                pluginId = "com.marmatsan.reporting"
            )
        )
        rootDir.writeBuildFile(
            path = "build-logic/dependencies",
            content = conventionPluginBuildFile(
                pluginName = "com.marmatsan.dependencies",
                implementationClass = "\${pluginName}.plugin.DependenciesPlugin"
            )
        )

        // WHEN
        val actualTree = GradleConventionPluginTreeReader().readPluginTree(rootDir)

        // THEN
        actualTree shouldBe PluginCatalogTree(
            roots = listOf(
                PluginCatalogNode(
                    id = "com",
                    children = listOf(
                        PluginCatalogNode(
                            id = "marmatsan",
                            children = listOf(
                                PluginCatalogNode(id = "analytics"),
                                PluginCatalogNode(id = "figmaDesignSync"),
                                PluginCatalogNode(id = "reporting")
                            )
                        )
                    )
                )
            )
        )
    }
})

private fun File.writeBuildFile(
    path: String,
    content: String
) {
    val directory = resolve(path)
    directory.mkdirs()
    directory.resolve("build.gradle.kts").writeText(content)
}

private fun conventionPluginBuildFile(
    pluginName: String,
    implementationClass: String
): String =
    """
    plugins {
        `kotlin-dsl`
        `java-gradle-plugin`
    }

    gradlePlugin {
        val pluginName = "$pluginName"
        plugins.register(pluginName) {
            id = pluginName
            implementationClass = "$implementationClass"
        }
    }
    """.trimIndent()

private fun literalIdConventionPluginBuildFile(pluginId: String): String =
    """
    plugins {
        `kotlin-dsl`
        `java-gradle-plugin`
    }

    gradlePlugin {
        plugins.register("reporting") {
            id = "$pluginId"
            implementationClass = "$pluginId.plugin.ReportingGradleConventionPlugin"
        }
    }
    """.trimIndent()
