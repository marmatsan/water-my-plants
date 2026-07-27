package com.marmatsan.figmaDocumentationSync.data.gradle.modules

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import java.io.File

internal class GradleProjectModulesReaderTest :
    FunSpec(
        {
            val temporaryDirectory =
                tempdir(
                    prefix = "gradle-project-modules-reader",
                )

            test("readModules returns root and included build modules") {
                // GIVEN
                val rootSettingsFile =
                    temporaryDirectory.settingsFile(
                        path = "modules/root/settings.gradle.kts",
                        content =
                            """
                            include(
                                ":app",
                                ":core:core_ui"
                            )
                            """.trimIndent(),
                    )
                val includedBuildSettingsFile =
                    temporaryDirectory.settingsFile(
                        path = "modules/included/settings.gradle.kts",
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
                    temporaryDirectory.settingsFile(
                        path = "standalone/root/settings.gradle.kts",
                        content = "",
                    )
                val includedBuildSettingsFile =
                    temporaryDirectory.settingsFile(
                        path = "standalone/included/settings.gradle.kts",
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
                    temporaryDirectory.settingsFile(
                        path = "catalog/root/settings.gradle.kts",
                        content = "",
                    )
                val includedBuildSettingsFile =
                    temporaryDirectory.settingsFile(
                        path = "catalog/included/settings.gradle.kts",
                        content =
                            """
                            include(
                                ":catalog-core",
                                ":catalog-gradle-plugin"
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
                        ":dependency-catalog:catalog-gradle-plugin",
                    )
            }
        },
    )

private fun File.settingsFile(
    path: String,
    content: String,
): File =
    resolve(path)
        .apply {
            parentFile.mkdirs()
            writeText(content)
        }
