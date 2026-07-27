package com.marmatsan.dependencies.gradle

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome

internal class DependencyCatalogSettingsPluginTest :
    FunSpec(
        {
            val temporaryDirectory =
                tempdir(
                    prefix = "dependency-catalog-settings",
                )

            test("registers repository trees as library and plugin catalogs") {
                val projectDirectory =
                    temporaryDirectory.resolve("consumer").apply {
                        mkdirs()
                    }
                projectDirectory.resolve("settings.gradle.kts").writeText(
                    """
                    import com.marmatsan.dependencies.catalog.api.DependencyCatalog
                    import com.marmatsan.dependencies.catalog.api.ResolvedDependencyCatalogProvider
                    import com.marmatsan.dependencies.catalog.api.LibraryCatalogEntry
                    import com.marmatsan.dependencies.catalog.api.LibraryCatalogNode
                    import com.marmatsan.dependencies.catalog.api.PluginCatalogNode
                    import java.io.File

                    plugins {
                        id("com.marmatsan.dependencyCatalog")
                    }

                    dependencyCatalog {
                        from(
                            provider =
                                object : ResolvedDependencyCatalogProvider {
                                    override fun resolved(rootDir: File) =
                                        DependencyCatalog(
                                            libraries =
                                                listOf(
                                                    LibraryCatalogNode(
                                                        group = "com",
                                                        children =
                                                            listOf(
                                                                LibraryCatalogNode(
                                                                    group = "example",
                                                                    entries =
                                                                        listOf(
                                                                            LibraryCatalogEntry.Artifact(
                                                                                name = "library",
                                                                                version = "1.2.3",
                                                                            ),
                                                                        ),
                                                                ),
                                                            ),
                                                    ),
                                                ),
                                            plugins =
                                                listOf(
                                                    PluginCatalogNode(
                                                        id = "com",
                                                        children =
                                                            listOf(
                                                                PluginCatalogNode(
                                                                    id = "example",
                                                                    children =
                                                                        listOf(
                                                                            PluginCatalogNode(
                                                                                id = "example-plugin",
                                                                                version = "2.0.0",
                                                                            ),
                                                                        ),
                                                                ),
                                                            ),
                                                    ),
                                                ),
                                        )
                                },
                        )
                    }

                    check(dependencyResolutionManagement.versionCatalogs.names == setOf("libs", "plugins"))

                    rootProject.name = "consumer"
                    """.trimIndent(),
                )
                projectDirectory.resolve("build.gradle.kts").writeText(
                    """
                    import org.gradle.api.artifacts.VersionCatalogsExtension

                    tasks.register("verifyCatalogs") {
                        doLast {
                            val catalogs = project.extensions.getByType<VersionCatalogsExtension>()
                            check(catalogs.named("libs").findLibrary("com.example.library").isPresent)
                            check(catalogs.named("plugins").findPlugin("com.example.example-plugin").isPresent)
                        }
                    }
                    """.trimIndent(),
                )

                val result =
                    GradleRunner
                        .create()
                        .withProjectDir(projectDirectory)
                        .withArguments(
                            "verifyCatalogs",
                            "--stacktrace",
                        ).withPluginClasspath()
                        .build()

                result.task(":verifyCatalogs")?.outcome shouldBe TaskOutcome.SUCCESS
            }
        },
    )
