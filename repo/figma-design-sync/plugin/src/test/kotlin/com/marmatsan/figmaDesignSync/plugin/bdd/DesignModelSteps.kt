package com.marmatsan.figmaDesignSync.plugin.bdd

import com.marmatsan.figmaDesignSync.domain.model.catalog.CatalogVersion
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogEntry
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogNode
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaDesignSync.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaDesignSync.domain.model.catalog.PluginCatalogTree
import com.marmatsan.figmaDesignSync.domain.model.modules.ModuleDependency
import com.marmatsan.figmaDesignSync.domain.model.versions.RepositoryVersionSection
import com.marmatsan.figmaDesignSync.domain.port.catalog.ProjectCatalogTreeSource
import com.marmatsan.figmaDesignSync.domain.port.catalog.ProjectCatalogTreesPort
import com.marmatsan.figmaDesignSync.domain.port.modules.ProjectModuleDependenciesPort
import com.marmatsan.figmaDesignSync.domain.port.modules.ProjectModuleDependenciesSource
import com.marmatsan.figmaDesignSync.domain.port.modules.ProjectModulesPort
import com.marmatsan.figmaDesignSync.domain.port.modules.ProjectModulesSource
import com.marmatsan.figmaDesignSync.domain.port.versions.RepositoryVersionsPort
import com.marmatsan.figmaDesignSync.domain.port.versions.VersionsFileSource
import com.marmatsan.figmaDesignSync.plugin.generator.FigmaDesignModelGenerationRequest
import com.marmatsan.figmaDesignSync.plugin.generator.FigmaDesignModelGenerationResult
import com.marmatsan.figmaDesignSync.plugin.generator.FigmaDesignModelGenerator
import com.marmatsan.figmaDesignSync.plugin.generator.FigmaDesignModelIncludedBuildSource
import io.cucumber.datatable.DataTable
import io.cucumber.java8.En
import io.cucumber.java8.StepDefinitionBody.A1
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import java.io.File
import java.time.Instant
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Suppress("ObjectLiteralToLambda")
class DesignModelSteps : En {

    private var repositoryVersionsAvailable = false
    private var repositoryCatalogTreesAvailable = false
    private var repositoryProjectModulesAvailable = false
    private var repositoryModuleDependenciesAvailable = false
    private lateinit var generator: FigmaDesignModelGenerator
    private lateinit var firstResult: FigmaDesignModelGenerationResult
    private lateinit var secondResult: FigmaDesignModelGenerationResult

    init {
        Given("repository versions are available") {
            repositoryVersionsAvailable = true
            configureGenerator()
        }

        Given("repository catalog trees are available") {
            repositoryCatalogTreesAvailable = true
            configureGenerator()
        }

        Given("repository project modules are available") {
            repositoryProjectModulesAvailable = true
            configureGenerator()
        }

        Given("repository module dependencies are available") {
            repositoryModuleDependenciesAvailable = true
            configureGenerator()
        }

        When(
            "the design model is generated at {instant}",
            object : A1<Instant> {
                override fun accept(generatedAt: Instant) {
                    generateFirstModel(generatedAt = generatedAt)
                }
            }
        )

        When(
            "the design model is generated again at {instant}",
            object : A1<Instant> {
                override fun accept(generatedAt: Instant) {
                    generateSecondModel(generatedAt = generatedAt)
                }
            }
        )

        When(
            "the design model is generated for git sha {word}",
            object : A1<String> {
                override fun accept(gitSha: String) {
                    generateFirstModel(gitSha = gitSha)
                }
            }
        )

        When(
            "the design model is generated again for git sha {word}",
            object : A1<String> {
                override fun accept(gitSha: String) {
                    generateSecondModel(gitSha = gitSha)
                }
            }
        )

        Then("the generated model contains repository metadata") {
            firstResult.model.keys shouldContainAll listOf(
                "schemaVersion",
                "branch",
                "gitSha",
                "generatedAt",
                "content",
                "modelHash"
            )
        }

        Then(
            "the generated model content contains:",
            object : A1<DataTable> {
                override fun accept(contentKeys: DataTable) {
                    firstResult.content.keys shouldContainAll contentKeys.asList()
                }
            }
        )

        Then("the version keys are sorted") {
            firstResult.content["versions"]
                ?.jsonObject
                ?.keys
                ?.toList() shouldBe listOf(
                    "activityComposeLibraryVersion",
                    "androidGradlePlugin",
                    "kotlinVersion",
                    "kspPluginVersion"
                )
        }

        Then("the version sections keep repository order") {
            firstResult.content["versionSections"]
                ?.jsonArray
                ?.map { section ->
                    section.jsonObject["name"]?.jsonPrimitive?.content
                } shouldBe listOf("Main project dependencies", "Libraries", "Plugins")
        }

        Then("the model hash is stored in the generated model") {
            firstResult.modelHash shouldBe firstResult.model["modelHash"]?.jsonPrimitive?.content
        }

        Then("both generated model hashes are equal") {
            firstResult.modelHash shouldBe secondResult.modelHash
        }

        Then("both generated model hashes are different") {
            (firstResult.modelHash == secondResult.modelHash) shouldBe false
        }
    }

    private val FigmaDesignModelGenerationResult.content: JsonObject
        get() = model["content"]!!.jsonObject

    private fun generateFirstModel(
        gitSha: String = "abc123",
        generatedAt: Instant = DEFAULT_GENERATED_AT
    ) {
        firstResult = generateModel(gitSha = gitSha, generatedAt = generatedAt)
    }

    private fun generateSecondModel(
        gitSha: String = "abc123",
        generatedAt: Instant = DEFAULT_GENERATED_AT
    ) {
        secondResult = generateModel(gitSha = gitSha, generatedAt = generatedAt)
    }

    private fun generateModel(
        gitSha: String,
        generatedAt: Instant
    ): FigmaDesignModelGenerationResult {
        requireRepositorySources()
        return generator.generate(
            request(
                gitSha = gitSha,
                generatedAt = generatedAt
            )
        )
    }

    private fun configureGenerator() {
        generator = FigmaDesignModelGenerator(
            repositoryVersionsPort = FakeRepositoryVersionsPort,
            projectCatalogTreesPort = FakeProjectCatalogTreesPort,
            projectModulesPort = FakeProjectModulesPort,
            projectModuleDependenciesPort = FakeProjectModuleDependenciesPort
        )
    }

    private fun requireRepositorySources() {
        check(repositoryVersionsAvailable) { "Repository versions are not available." }
        check(repositoryCatalogTreesAvailable) { "Repository catalog trees are not available." }
        check(repositoryProjectModulesAvailable) { "Repository project modules are not available." }
        check(repositoryModuleDependenciesAvailable) { "Repository module dependencies are not available." }
    }

    private fun request(
        gitSha: String = "abc123",
        generatedAt: Instant = DEFAULT_GENERATED_AT
    ): FigmaDesignModelGenerationRequest =
        FigmaDesignModelGenerationRequest(
            branch = "main",
            gitSha = gitSha,
            generatedAt = generatedAt,
            versionsFile = File("versions.properties"),
            rootSettingsFile = File("settings.gradle.kts"),
            projectRootDirectory = File("."),
            includedBuilds = listOf(
                FigmaDesignModelIncludedBuildSource(
                    modelName = "gradlePlugins",
                    settingsFile = File("repo/gradle-plugins/settings.gradle.kts"),
                    rootDirectory = File("repo/gradle-plugins"),
                    modulePathPrefix = ":gradle-plugins",
                    publishesCatalogs = true,
                    publishesConventionPlugins = true
                )
            )
        )

    private companion object {
        val DEFAULT_GENERATED_AT: Instant = Instant.parse("2026-06-19T10:15:30Z")
    }
}

private object FakeRepositoryVersionsPort : RepositoryVersionsPort {
    override fun readVersions(source: VersionsFileSource): Map<String, String> =
        mapOf(
            "activityComposeLibraryVersion" to "1.13.0",
            "kotlinVersion" to "2.4.0",
            "androidGradlePlugin" to "9.2.1",
            "kspPluginVersion" to "2.3.9"
        )

    override fun readVersionSections(source: VersionsFileSource): List<RepositoryVersionSection> =
        listOf(
            RepositoryVersionSection(
                name = "Main project dependencies",
                versions = mapOf(
                    "androidGradlePlugin" to "9.2.1",
                    "kotlinVersion" to "2.4.0"
                )
            ),
            RepositoryVersionSection(
                name = "Libraries",
                versions = mapOf("activityComposeLibraryVersion" to "1.13.0")
            ),
            RepositoryVersionSection(
                name = "Plugins",
                versions = mapOf("kspPluginVersion" to "2.3.9")
            )
        )
}

private object FakeProjectCatalogTreesPort : ProjectCatalogTreesPort {
    override fun readLibraryTree(source: ProjectCatalogTreeSource): LibraryCatalogTree =
        LibraryCatalogTree(
            roots = listOf(
                LibraryCatalogNode(
                    group = "org.jetbrains.kotlin",
                    entries = listOf(
                        LibraryCatalogEntry.Artifact(
                            artifact = "kotlin-stdlib",
                            version = CatalogVersion("2.4.0"),
                            requiredByModules = listOf(":app")
                        )
                    )
                )
            )
        )

    override fun readPluginTree(source: ProjectCatalogTreeSource): PluginCatalogTree =
        PluginCatalogTree(
            roots = listOf(
                PluginCatalogNode(
                    id = "org.jetbrains.kotlin.android",
                    version = CatalogVersion("2.4.0"),
                    appliedToModules = listOf(":app")
                )
            )
        )
}

private object FakeProjectModulesPort : ProjectModulesPort {
    override fun readModules(source: ProjectModulesSource): Set<String> =
        setOf(":onboarding:ui", ":app", ":core:ui")
}

private object FakeProjectModuleDependenciesPort : ProjectModuleDependenciesPort {
    override fun readModuleDependencies(source: ProjectModuleDependenciesSource): Set<ModuleDependency> =
        setOf(
            ModuleDependency(
                dependentModule = ":app",
                dependencyModule = ":core:ui"
            )
        )
}
