package com.marmatsan.figmaDesignSync.data.gradle.catalog

import com.marmatsan.figmaDesignSync.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaDesignSync.domain.model.catalog.PluginCatalogTree
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files

internal class GradlePluginTreeReaderTest : FunSpec({

    test("readPluginTree detects regular Gradle plugins and ignores convention plugins") {
        // GIVEN
        val rootDir = Files.createTempDirectory("gradle-plugin-tree").toFile()
        rootDir.writeGradlePluginBuildFile(
            path = "build-logic/figmaDesignSync/plugin",
            pluginName = "com.marmatsan.figmaDesignSync",
            implementationClass = "\${pluginName}.plugin.gradle.FigmaDesignSyncGradlePlugin"
        )
        rootDir.writeGradlePluginBuildFile(
            path = "build-logic/analytics",
            pluginName = "com.marmatsan.analytics",
            implementationClass = "\${pluginName}.plugin.AnalyticsGradlePlugin"
        )
        rootDir.writeGradlePluginBuildFile(
            path = "build-logic/android",
            pluginName = "com.marmatsan.android",
            implementationClass = "\${pluginName}.plugin.AndroidGradleConventionPlugin"
        )

        // WHEN
        val actualTree = GradlePluginTreeReader().readPluginTree(
            rootDir = rootDir,
            includedPluginIds = setOf("com.marmatsan.figmaDesignSync"),
            usageByPluginId = emptyMap()
        )

        // THEN
        actualTree shouldBe PluginCatalogTree(
            roots = listOf(
                PluginCatalogNode(
                    id = "com",
                    children = listOf(
                        PluginCatalogNode(
                            id = "marmatsan",
                            children = listOf(
                                PluginCatalogNode(
                                    id = "figmaDesignSync"
                                )
                            )
                        )
                    )
                )
            )
        )
    }
})

private fun File.writeGradlePluginBuildFile(
    path: String,
    pluginName: String,
    implementationClass: String
) {
    val directory = resolve(path)
    directory.mkdirs()
    directory.resolve("build.gradle.kts").writeText(
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
    )
}
