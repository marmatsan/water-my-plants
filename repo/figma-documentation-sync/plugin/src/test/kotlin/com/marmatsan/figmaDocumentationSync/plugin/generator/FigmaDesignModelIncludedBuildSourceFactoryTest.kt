package com.marmatsan.figmaDocumentationSync.plugin.generator

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.io.File

internal class FigmaDesignModelIncludedBuildSourceFactoryTest :
    FunSpec(
        {
            test("creates sources from aligned serializable task inputs") {
                val settingsFile = File("repo/example/settings.gradle.kts").absoluteFile
                val rootDirectory = settingsFile.parentFile

                FigmaDesignModelIncludedBuildSourceFactory()
                    .create(
                        settingsFilePaths = listOf(settingsFile.path),
                        rootDirectoryPaths = listOf(rootDirectory.path),
                        modelNames = listOf("example"),
                        modulePathPrefixes = listOf(":example"),
                        publishesCatalogs = listOf(false),
                        publishesConventionPlugins = listOf(true)
                    ).single() shouldBe
                    FigmaDesignModelIncludedBuildSource(
                        modelName = "example",
                        settingsFile = settingsFile,
                        rootDirectory = rootDirectory,
                        modulePathPrefix = ":example",
                        publishesCatalogs = false,
                        publishesConventionPlugins = true
                    )
            }

            test("rejects included-build input lists with different sizes") {
                val error =
                    shouldThrow<IllegalArgumentException> {
                        FigmaDesignModelIncludedBuildSourceFactory().create(
                            settingsFilePaths = listOf("settings.gradle.kts"),
                            rootDirectoryPaths = emptyList(),
                            modelNames = listOf("example"),
                            modulePathPrefixes = listOf(":example"),
                            publishesCatalogs = listOf(false),
                            publishesConventionPlugins = listOf(false)
                        )
                    }

                error.message shouldContain "Included-build task inputs must have matching sizes"
            }
        }
    )
