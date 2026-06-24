package com.marmatsan.figmaDesignSync.plugin.bdd.steps

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
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import java.io.File
import java.time.Instant
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class FigmaDesignModelGenerationSteps {

    private lateinit var generator: FigmaDesignModelGenerator
    private lateinit var firstResult: FigmaDesignModelGenerationResult
    private lateinit var secondResult: FigmaDesignModelGenerationResult

    @Given("repository model sources contain versions, catalogs, modules, and module dependencies")
    fun repositoryModelSourcesContainVersionsCatalogsModulesAndModuleDependencies() {
        generator = FigmaDesignModelGenerator(
            repositoryVersionsPort = FakeRepositoryVersionsPort,
            projectCatalogTreesPort = FakeProjectCatalogTreesPort,
            projectModulesPort = FakeProjectModulesPort,
            projectModuleDependenciesPort = FakeProjectModuleDependenciesPort
        )
    }

    @When("the design model is generated at {string}")
    fun theDesignModelIsGeneratedAt(generatedAt: String) {
        firstResult = generator.generate(
            request(generatedAt = Instant.parse(generatedAt))
        )
    }

    @When("the design model is generated again at {string}")
    fun theDesignModelIsGeneratedAgainAt(generatedAt: String) {
        secondResult = generator.generate(
            request(generatedAt = Instant.parse(generatedAt))
        )
    }

    @When("the design model is generated for git sha {string}")
    fun theDesignModelIsGeneratedForGitSha(gitSha: String) {
        firstResult = generator.generate(
            request(gitSha = gitSha)
        )
    }

    @When("the design model is generated again for git sha {string}")
    fun theDesignModelIsGeneratedAgainForGitSha(gitSha: String) {
        secondResult = generator.generate(
            request(gitSha = gitSha)
        )
    }

    @Then("the generated model contains versions, version sections, catalogs, modules, and module dependencies")
    fun theGeneratedModelContainsVersionsVersionSectionsCatalogsModulesAndModuleDependencies() {
        firstResult.model.keys shouldContainAll listOf(
            "schemaVersion",
            "branch",
            "gitSha",
            "generatedAt",
            "content",
            "modelHash"
        )

        firstResult.content.keys shouldContainAll listOf(
            "versions",
            "versionSections",
            "catalogs",
            "modules",
            "moduleDependencies"
        )
    }

    @Then("the version keys are sorted")
    fun theVersionKeysAreSorted() {
        firstResult.content["versions"]
            ?.jsonObject
            ?.keys
            ?.toList() shouldBe listOf("androidGradlePlugin", "kotlinVersion")
    }

    @Then("the version sections keep repository order")
    fun theVersionSectionsKeepRepositoryOrder() {
        firstResult.content["versionSections"]
            ?.jsonArray
            ?.map { section ->
                section.jsonObject["name"]?.jsonPrimitive?.content
            } shouldBe listOf("Main project dependencies", "Libraries")
    }

    @Then("the model hash is stored in the generated model")
    fun theModelHashIsStoredInTheGeneratedModel() {
        firstResult.modelHash shouldBe firstResult.model["modelHash"]?.jsonPrimitive?.content
    }

    @Then("both generated model hashes are equal")
    fun bothGeneratedModelHashesAreEqual() {
        firstResult.modelHash shouldBe secondResult.modelHash
    }

    @Then("both generated model hashes are different")
    fun bothGeneratedModelHashesAreDifferent() {
        (firstResult.modelHash == secondResult.modelHash) shouldBe false
    }

    private val FigmaDesignModelGenerationResult.content: JsonObject
        get() = model["content"]!!.jsonObject

    private fun request(
        gitSha: String = "abc123",
        generatedAt: Instant = Instant.parse("2026-06-19T10:15:30Z")
    ): FigmaDesignModelGenerationRequest =
        FigmaDesignModelGenerationRequest(
            branch = "main",
            gitSha = gitSha,
            generatedAt = generatedAt,
            versionsFile = File("versions.properties"),
            rootSettingsFile = File("settings.gradle.kts"),
            buildLogicSettingsFile = File("build-logic/settings.gradle.kts"),
            projectRootDirectory = File("."),
            buildLogicRootDirectory = File("build-logic")
        )
}

private object FakeRepositoryVersionsPort : RepositoryVersionsPort {
    override fun readVersions(source: VersionsFileSource): Map<String, String> =
        mapOf(
            "kotlinVersion" to "2.4.0",
            "androidGradlePlugin" to "9.2.1"
        )

    override fun readVersionSections(source: VersionsFileSource): List<RepositoryVersionSection> =
        listOf(
            RepositoryVersionSection(
                name = "Main project dependencies",
                versions = mapOf("androidGradlePlugin" to "9.2.1")
            ),
            RepositoryVersionSection(
                name = "Libraries",
                versions = mapOf("kotlinVersion" to "2.4.0")
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
