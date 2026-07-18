package com.marmatsan.figmaDesignSync.plugin.bdd

import com.marmatsan.figmaDesignSync.domain.model.catalog.CatalogVersion
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogEntry
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogNode
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaDesignSync.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaDesignSync.domain.model.catalog.PluginCatalogTree
import com.marmatsan.figmaDesignSync.domain.model.ci.CiExternalTopology
import com.marmatsan.figmaDesignSync.domain.model.ci.CiNode
import com.marmatsan.figmaDesignSync.domain.model.ci.CiWindowsRuntime
import com.marmatsan.figmaDesignSync.domain.model.ci.CiConfiguration
import com.marmatsan.figmaDesignSync.domain.model.ci.CiJob
import com.marmatsan.figmaDesignSync.domain.model.ci.CiPipeline
import com.marmatsan.figmaDesignSync.domain.model.modules.ModuleDependency
import com.marmatsan.figmaDesignSync.domain.model.versions.RepositoryVersionSection
import com.marmatsan.figmaDesignSync.domain.port.catalog.ProjectCatalogTreeSource
import com.marmatsan.figmaDesignSync.domain.port.catalog.ProjectCatalogTreesPort
import com.marmatsan.figmaDesignSync.domain.port.ci.CiExternalTopologyPort
import com.marmatsan.figmaDesignSync.domain.port.ci.CiExternalTopologySource
import com.marmatsan.figmaDesignSync.domain.port.ci.CiWindowsRuntimePort
import com.marmatsan.figmaDesignSync.domain.port.ci.CiWindowsRuntimeSource
import com.marmatsan.figmaDesignSync.domain.port.ci.CiConfigurationPort
import com.marmatsan.figmaDesignSync.domain.port.ci.CiGeneratedConfigurationSource
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
import java.time.LocalDate
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
    private var externalCiTopologyAvailable = false
    private var windowsCiRuntimeAvailable = false
    private var effectiveTeamCityConfigurationAvailable = false
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

        Given("the external CI topology is available") {
            externalCiTopologyAvailable = true
            configureGenerator()
        }

        Given("the Windows CI runtime is available") {
            windowsCiRuntimeAvailable = true
            configureGenerator()
        }

        Given("the effective TeamCity configuration is available") {
            effectiveTeamCityConfigurationAvailable = true
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
                    "androidGradlePluginVersion",
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

        Then("the CI model contains external topology Windows runtime and effective TeamCity configuration") {
            firstResult.content["ci"]!!
                .jsonObject
                .keys shouldContainAll listOf("externalTopology", "windowsRuntime", "teamCity")
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
            projectModuleDependenciesPort = FakeProjectModuleDependenciesPort,
            ciExternalTopologyPort = FakeCiExternalTopologyPort,
            ciWindowsRuntimePort = FakeCiWindowsRuntimePort,
            ciConfigurationPort = FakeCiConfigurationPort
        )
    }

    private fun requireRepositorySources() {
        check(repositoryVersionsAvailable) { "Repository versions are not available." }
        check(repositoryCatalogTreesAvailable) { "Repository catalog trees are not available." }
        check(repositoryProjectModulesAvailable) { "Repository project modules are not available." }
        check(repositoryModuleDependenciesAvailable) { "Repository module dependencies are not available." }
        check(externalCiTopologyAvailable) { "External CI topology is not available." }
        check(windowsCiRuntimeAvailable) { "Windows CI runtime is not available." }
        check(effectiveTeamCityConfigurationAvailable) { "Effective TeamCity configuration is not available." }
    }

    private fun request(
        gitSha: String = "abc123",
        generatedAt: Instant = DEFAULT_GENERATED_AT
    ): FigmaDesignModelGenerationRequest =
        FigmaDesignModelGenerationRequest(
            branch = "main",
            gitSha = gitSha,
            generatedAt = generatedAt,
            primaryCatalogModelName = "waterMyPlants",
            dependencyCatalogProviderClassName = "example.DependencyCatalogProvider",
            ciDocumentationEnabled = true,
            ciConfigurationModelName = "teamCity",
            ciConfigurationProviderClassName = "example.CiConfigurationProvider",
            versionsFile = File("versions.properties"),
            rootSettingsFile = File("settings.gradle.kts"),
            ciExternalTopologyFile = File("docs/ci/external-topology.yaml"),
            ciWindowsRuntimeFile = File("docs/ci/windows-runtime.yaml"),
            ciGeneratedConfigurationDirectory = File(".teamcity/target/generated-configs"),
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
            "androidGradlePluginVersion" to "9.2.1",
            "kspPluginVersion" to "2.3.9"
        )

    override fun readVersionSections(source: VersionsFileSource): List<RepositoryVersionSection> =
        listOf(
            RepositoryVersionSection(
                name = "Main project dependencies",
                versions = mapOf(
                    "androidGradlePluginVersion" to "9.2.1",
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

private object FakeCiExternalTopologyPort : CiExternalTopologyPort {
    override fun readTopology(source: CiExternalTopologySource): CiExternalTopology =
        CiExternalTopology(
            schemaVersion = 1,
            validation = CiExternalTopology.Validation(
                lastValidatedOn = LocalDate.parse("2026-07-14"),
                warnAfterDays = 90
            ),
            nodes = listOf(
                CiNode(
                    id = "operator",
                    type = CiNode.Type.Actor,
                    name = "Operator",
                    description = "Initiates manual CI actions."
                )
            ),
            connections = emptyList()
        )
}

private object FakeCiWindowsRuntimePort : CiWindowsRuntimePort {
    override fun readRuntime(source: CiWindowsRuntimeSource): CiWindowsRuntime =
        CiWindowsRuntime(
            schemaVersion = 1,
            validation = CiWindowsRuntime.Validation(
                lastValidatedOn = LocalDate.parse("2026-07-16"),
                warnAfterDays = 90
            ),
            platform = "Windows",
            services = listOf(
                CiWindowsRuntime.Service(
                    id = "teamcity-server",
                    name = "TeamCity Server",
                    description = "Hosts TeamCity.",
                    service = "TeamCity",
                    startup = "Automatic",
                    identity = "NT SERVICE\\TeamCity"
                )
            )
        )
}

private object FakeCiConfigurationPort : CiConfigurationPort {
    override fun readConfiguration(source: CiGeneratedConfigurationSource): CiConfiguration =
        CiConfiguration(
            pipelines = listOf(
                CiPipeline(
                    id = "Root_Ci",
                    name = "CI",
                    triggers = emptyList(),
                    jobs = listOf(
                        CiJob(
                            id = "verify",
                            name = "Verify",
                            steps = emptyList(),
                            repositoryIds = emptyList(),
                            artifacts = emptyList(),
                            dependencies = emptyList(),
                            publishedChecks = emptyList()
                        )
                    )
                )
            ),
            vcsRoots = emptyList()
        )
}
