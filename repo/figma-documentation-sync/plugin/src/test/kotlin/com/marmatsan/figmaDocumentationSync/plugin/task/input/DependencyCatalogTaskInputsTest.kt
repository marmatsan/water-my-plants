package com.marmatsan.figmaDocumentationSync.plugin.task.input

import com.marmatsan.figmaDocumentationSync.data.json.catalog.DependencyCatalogTreesJson
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.DependencyCatalogTrees
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogTree
import com.marmatsan.figmaDocumentationSync.domain.port.catalog.ProjectCatalogTreeSource
import com.marmatsan.unitTest.dsl.given
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

internal class DependencyCatalogTaskInputsTest :
    FunSpec(
        {
            test("resolves serialized trees as a preconfigured catalog source") {
                given {
                    val inputs = dependencyCatalogTaskInputs()
                    inputs.dependencyCatalogTreesJson.set(
                        DependencyCatalogTreesJson.encode(dependencyCatalogTrees())
                    )
                    inputs
                }.whenever { inputs ->
                    inputs.resolveDependencyCatalogTreeSource(
                        projectRootDirectory = File("project"),
                        conventionPluginIncludedBuilds = emptyList()
                    )
                }.then { source ->
                    source shouldBe
                        ProjectCatalogTreeSource.PreconfiguredVersionAliases(
                            trees = dependencyCatalogTrees()
                        )
                }
            }

            test("resolves the legacy provider while migration compatibility remains") {
                given {
                    dependencyCatalogTaskInputs().apply {
                        dependencyCatalogProviderClassName.set("com.example.CatalogProvider")
                    }
                }.whenever { inputs ->
                    inputs.resolveDependencyCatalogTreeSource(
                        projectRootDirectory = File("project"),
                        conventionPluginIncludedBuilds = emptyList()
                    )
                }.then { source ->
                    source shouldBe
                        ProjectCatalogTreeSource.DependenciesDslVersionAliases(
                            rootDirPath = File("project").absolutePath,
                            providerClassName = "com.example.CatalogProvider",
                            conventionPluginIncludedBuilds = emptyList()
                        )
                }
            }

            test("rejects a missing catalog source") {
                given {
                    dependencyCatalogTaskInputs()
                }.whenever { inputs ->
                    shouldThrow<GradleException> {
                        inputs.resolveDependencyCatalogTreeSource(
                            projectRootDirectory = File("project"),
                            conventionPluginIncludedBuilds = emptyList()
                        )
                    }
                }.then { failure ->
                    failure.message shouldContain "Configure exactly one"
                }
            }

            test("rejects two catalog sources") {
                given {
                    dependencyCatalogTaskInputs().apply {
                        dependencyCatalogProviderClassName.set("com.example.CatalogProvider")
                        dependencyCatalogTreesJson.set(
                            DependencyCatalogTreesJson.encode(dependencyCatalogTrees())
                        )
                    }
                }.whenever { inputs ->
                    shouldThrow<GradleException> {
                        inputs.resolveDependencyCatalogTreeSource(
                            projectRootDirectory = File("project"),
                            conventionPluginIncludedBuilds = emptyList()
                        )
                    }
                }.then { failure ->
                    failure.message shouldContain "Configure exactly one"
                }
            }
        }
    )

private fun dependencyCatalogTaskInputs(): DependencyCatalogTaskInputs {
    val objects = ProjectBuilder.builder().build().objects
    return object : DependencyCatalogTaskInputs {
        override val dependencyCatalogProviderClassName: Property<String> =
            objects.property(String::class.java)
        override val dependencyCatalogTreesJson: Property<String> =
            objects.property(String::class.java)
    }
}

private fun dependencyCatalogTrees(): DependencyCatalogTrees =
    DependencyCatalogTrees(
        libraries =
            LibraryCatalogTree(
                roots = emptyList()
            ),
        plugins =
            PluginCatalogTree(
                roots =
                    listOf(
                        PluginCatalogNode(
                            id = "com",
                            children =
                                listOf(
                                    PluginCatalogNode(
                                        id = "example"
                                    )
                                )
                        )
                    )
            )
    )
