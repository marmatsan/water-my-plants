package com.marmatsan.dependencies.gradle

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.nio.file.Files

internal class DependencyCatalogSettingsPluginTest :
    FunSpec(
        {
            test("registers repository trees as library and plugin catalogs") {
                val projectDirectory = Files.createTempDirectory("dependency-catalog-consumer").toFile()
                projectDirectory.resolve("settings.gradle.kts").writeText(
                    """
                    import com.marmatsan.dependencies.catalog.api.DependencyCatalog
                    import com.marmatsan.dependencies.catalog.api.DependencyCatalogProvider
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
                                object : DependencyCatalogProvider {
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

                                    override fun withVersionAliases() = resolved(rootDir = File("."))
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
