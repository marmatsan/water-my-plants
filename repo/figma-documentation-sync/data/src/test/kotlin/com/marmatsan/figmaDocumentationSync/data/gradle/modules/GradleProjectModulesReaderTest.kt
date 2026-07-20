package com.marmatsan.figmaDocumentationSync.data.gradle.modules

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files

internal class GradleProjectModulesReaderTest :
    FunSpec(
        {

            test("readModules returns root and included build modules") {
                // GIVEN
                val rootSettingsFile =
                    settingsFile(
                        content =
                            """
                            include(
                                ":app",
                                ":core:core_ui"
                            )
                            """.trimIndent(),
                    )
                val includedBuildSettingsFile =
                    settingsFile(
                        content =
                            """
                            include(
                                ":android",
                                ":catalog:data"
                            )
                            """.trimIndent(),
                    )
                includedBuildSettingsFile.parentFile
                    .resolve(
                        relative = "catalog",
                    ).mkdirs()

                // WHEN
                val modules =
                    GradleProjectModulesReader().readModules(
                        rootSettingsFile = rootSettingsFile,
                        includedBuilds =
                            listOf(
                                GradleProjectModulesReader.IncludedBuild(
                                    settingsFile = includedBuildSettingsFile,
                                    modulePathPrefix = ":gradle-plugins",
                                ),
                            ),
                    )

                // THEN
                modules shouldBe
                    setOf(
                        ":app",
                        ":gradle-plugins:android",
                        ":gradle-plugins:catalog",
                        ":gradle-plugins:catalog:data",
                        ":core:core_ui",
                    )
            }

            test("readModules returns standalone included build root module") {
                // GIVEN
                val rootSettingsFile =
                    settingsFile(
                        content = "",
                    )
                val includedBuildSettingsFile =
                    settingsFile(
                        content = "",
                    )
                includedBuildSettingsFile.parentFile
                    .resolve(
                        relative = "build.gradle.kts",
                    ).writeText("")

                // WHEN
                val modules =
                    GradleProjectModulesReader().readModules(
                        rootSettingsFile = rootSettingsFile,
                        includedBuilds =
                            listOf(
                                GradleProjectModulesReader.IncludedBuild(
                                    settingsFile = includedBuildSettingsFile,
                                    modulePathPrefix = ":dependency-catalog",
                                ),
                            ),
                    )

                // THEN
                modules shouldBe setOf(":dependency-catalog")
            }

            test("readModules returns dependency catalog submodules without a root module") {
                // GIVEN
                val rootSettingsFile =
                    settingsFile(
                        content = "",
                    )
                val includedBuildSettingsFile =
                    settingsFile(
                        content =
                            """
                            include(
                                ":catalog-core",
                                ":water-my-plants-catalog"
                            )
                            """.trimIndent(),
                    )

                // WHEN
                val modules =
                    GradleProjectModulesReader().readModules(
                        rootSettingsFile = rootSettingsFile,
                        includedBuilds =
                            listOf(
                                GradleProjectModulesReader.IncludedBuild(
                                    settingsFile = includedBuildSettingsFile,
                                    modulePathPrefix = ":dependency-catalog",
                                ),
                            ),
                    )

                // THEN
                modules shouldBe
                    setOf(
                        ":dependency-catalog:catalog-core",
                        ":dependency-catalog:water-my-plants-catalog",
                    )
            }
        },
    )

private fun settingsFile(
    content: String,
): File =
    Files
        .createTempDirectory("gradle-project-modules-reader")
        .resolve(
            "settings.gradle.kts",
        ).toFile()
        .apply {
            writeText(content)
        }
