package com.marmatsan.dependencies.gradle.tree

import com.marmatsan.unitTest.dsl.given
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome

internal class TreeDependencyCatalogSettingsPluginTest :
    FunSpec(
        {
            test("registers compact library and plugin trees with versions owned by the consuming settings") {
                given {
                    tempdir(
                        prefix = "tree-dependency-catalog-settings",
                    ).apply {
                        resolve("versions.properties").writeText(
                            """
                            exampleLibraryVersion=1.2.3
                            examplePluginVersion=2.0.0
                            """.trimIndent(),
                        )
                        resolve("settings.gradle.kts").writeText(
                            """
                            plugins {
                                id("com.marmatsan.dependencyCatalog.tree")
                            }

                            dependencyCatalogTree {
                                libraries {
                                    library(
                                        group = "com.example.tools",
                                        artifact = "tools-core",
                                        version = version("exampleLibraryVersion"),
                                    )
                                    library(
                                        group = "com.example.format",
                                        artifact = "format-core",
                                        version = version("exampleLibraryVersion"),
                                    )
                                    library(
                                        group = "tools",
                                        artifact = "core",
                                        version = version("exampleLibraryVersion"),
                                    )
                                }

                                plugins {
                                    plugin(
                                        id = "com.example.quality",
                                        version = version("examplePluginVersion"),
                                    )
                                    plugin(
                                        id = "com.example.format",
                                        version = version("examplePluginVersion"),
                                    )
                                    plugin(
                                        id = "quality",
                                        version = version("examplePluginVersion"),
                                    )
                                }
                            }

                            rootProject.name = "tree-consumer"
                            """.trimIndent(),
                        )
                        resolve("build.gradle.kts").writeText(
                            """
                            import org.gradle.api.artifacts.VersionCatalogsExtension

                            tasks.register("verifyCatalogs") {
                                doLast {
                                    val catalogs = project.extensions.getByType<VersionCatalogsExtension>()
                                    check(catalogs.named("libs").findLibrary("com.example.tools.core").isPresent)
                                    check(catalogs.named("libs").findLibrary("com.example.format.core").isPresent)
                                    check(catalogs.named("libs").findLibrary("tools.core").isPresent)
                                    check(catalogs.named("plugins").findPlugin("com.example.quality").isPresent)
                                    check(catalogs.named("plugins").findPlugin("com.example.format").isPresent)
                                    check(catalogs.named("plugins").findPlugin("quality").isPresent)
                                }
                            }
                            """.trimIndent(),
                        )
                    }
                }.whenever { projectDirectory ->
                    GradleRunner
                        .create()
                        .withProjectDir(projectDirectory)
                        .withArguments(
                            "verifyCatalogs",
                            "--stacktrace",
                        ).withPluginClasspath()
                        .build()
                }.then { result ->
                    result.task(":verifyCatalogs")?.outcome shouldBe TaskOutcome.SUCCESS
                }
            }
        },
    )
