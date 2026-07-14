package com.marmatsan.figmaDesignSync.plugin.generator

import com.marmatsan.figmaDesignSync.domain.model.catalog.CatalogVersion
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogEntry
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogEntry.ConventionPluginConfigurationUsage
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogEntry.ConventionPluginUsage
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogNode
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaDesignSync.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaDesignSync.domain.model.catalog.PluginCatalogTree
import com.marmatsan.figmaDesignSync.domain.model.ci.CiExternalTopology
import com.marmatsan.figmaDesignSync.domain.model.ci.CiNode
import com.marmatsan.figmaDesignSync.domain.model.ci.TeamCityConfiguration
import com.marmatsan.figmaDesignSync.domain.model.ci.TeamCityJob
import com.marmatsan.figmaDesignSync.domain.model.ci.TeamCityPipeline
import com.marmatsan.figmaDesignSync.domain.model.modules.ModuleDependency
import com.marmatsan.figmaDesignSync.domain.model.versions.RepositoryVersionSection
import com.marmatsan.figmaDesignSync.domain.port.catalog.ProjectCatalogTreeSource
import com.marmatsan.figmaDesignSync.domain.port.catalog.ProjectCatalogTreesPort
import com.marmatsan.figmaDesignSync.domain.port.ci.CiExternalTopologyPort
import com.marmatsan.figmaDesignSync.domain.port.ci.CiExternalTopologySource
import com.marmatsan.figmaDesignSync.domain.port.ci.TeamCityConfigurationPort
import com.marmatsan.figmaDesignSync.domain.port.ci.TeamCityGeneratedConfigurationSource
import com.marmatsan.figmaDesignSync.domain.port.modules.ProjectModuleDependenciesPort
import com.marmatsan.figmaDesignSync.domain.port.modules.ProjectModuleDependenciesSource
import com.marmatsan.figmaDesignSync.domain.port.modules.ProjectModulesPort
import com.marmatsan.figmaDesignSync.domain.port.modules.ProjectModulesSource
import com.marmatsan.figmaDesignSync.domain.port.versions.RepositoryVersionsPort
import com.marmatsan.figmaDesignSync.domain.port.versions.VersionsFileSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.time.Instant
import java.time.LocalDate
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal class FigmaDesignModelGeneratorTest : FunSpec({

    test("generate keeps the same model hash when only generatedAt changes") {
        // GIVEN
        val generator = generator()

        // WHEN
        val first = generator.generate(request(generatedAt = Instant.parse("2026-06-19T10:15:30Z")))
        val second = generator.generate(request(generatedAt = Instant.parse("2026-06-19T10:16:30Z")))

        // THEN
        first.modelHash shouldBe second.modelHash
        first.model["generatedAt"]?.jsonPrimitive?.content shouldBe "2026-06-19T10:15:30Z"
        second.model["generatedAt"]?.jsonPrimitive?.content shouldBe "2026-06-19T10:16:30Z"
    }

    test("generate keeps the same model hash when only gitSha changes") {
        // GIVEN
        val generator = generator()

        // WHEN
        val first = generator.generate(request(gitSha = "abc123"))
        val second = generator.generate(request(gitSha = "def456"))

        // THEN
        first.modelHash shouldBe first.model["modelHash"]?.jsonPrimitive?.content
        second.modelHash shouldBe second.model["modelHash"]?.jsonPrimitive?.content
        first.modelHash shouldBe second.modelHash
    }

    test("generate writes sorted top-level content") {
        // GIVEN
        val generator = generator()

        // WHEN
        val result = generator.generate(request())

        // THEN
        result.model["content"]?.jsonObject
            ?.get("versions")
            ?.jsonObject
            ?.keys
            ?.toList() shouldBe listOf(
                "activityComposeLibraryVersion",
                "androidGradlePluginVersion",
                "kotlinVersion",
                "kspPluginVersion"
            )
    }

    test("generate writes version sections in repository order") {
        // GIVEN
        val generator = generator()

        // WHEN
        val result = generator.generate(request())

        // THEN
        val sections = result.model["content"]
            ?.jsonObject
            ?.get("versionSections")
            ?.jsonArray

        sections?.map { section ->
            section.jsonObject["name"]?.jsonPrimitive?.content
        } shouldBe listOf("Main project dependencies", "Libraries", "Plugins")
    }

    test("generate writes convention plugin provenance for library artifacts") {
        // GIVEN
        val generator = generator()

        // WHEN
        val result = generator.generate(request())

        // THEN
        val usages = result.model["content"]
            ?.jsonObject
            ?.get("catalogs")
            ?.jsonObject
            ?.get("waterMyPlants")
            ?.jsonObject
            ?.get("libraries")
            ?.jsonArray
            ?.single()
            ?.jsonObject
            ?.get("entries")
            ?.jsonArray
            ?.single()
            ?.jsonObject
            ?.get("providedByConventionPlugins")
            ?.jsonArray

        usages?.map { usage ->
            val usageObject = usage.jsonObject
            Triple(
                usageObject["pluginId"]?.jsonPrimitive?.content,
                usageObject["pluginModule"]?.jsonPrimitive?.content,
                usageObject["requiredByModules"]?.jsonArray?.map { module -> module.jsonPrimitive.content }
            )
        } shouldBe listOf(
            Triple(
                "com.marmatsan.compose",
                ":gradle-plugins:compose",
                listOf(":app", ":core:ui")
            )
        )
    }

    test("generate writes convention plugin configuration usage for library artifacts") {
        // GIVEN
        val generator = generator()

        // WHEN
        val result = generator.generate(request())

        // THEN
        val usages = result.model["content"]
            ?.jsonObject
            ?.get("catalogs")
            ?.jsonObject
            ?.get("waterMyPlants")
            ?.jsonObject
            ?.get("libraries")
            ?.jsonArray
            ?.single()
            ?.jsonObject
            ?.get("entries")
            ?.jsonArray
            ?.single()
            ?.jsonObject
            ?.get("configuredByConventionPlugins")
            ?.jsonArray

        usages?.map { usage ->
            val usageObject = usage.jsonObject
            Triple(
                usageObject["pluginId"]?.jsonPrimitive?.content,
                usageObject["pluginModule"]?.jsonPrimitive?.content,
                usageObject["target"]?.jsonPrimitive?.content
            )
        } shouldBe listOf(
            Triple(
                "com.marmatsan.kotlin",
                ":gradle-plugins:kotlin",
                "kotlin.compiler.classpath"
            )
        )
    }

    test("generate writes convention plugin provenance for plugins") {
        // GIVEN
        val generator = generator()

        // WHEN
        val result = generator.generate(request())

        // THEN
        val usages = result.model["content"]
            ?.jsonObject
            ?.get("catalogs")
            ?.jsonObject
            ?.get("waterMyPlants")
            ?.jsonObject
            ?.get("plugins")
            ?.jsonArray
            ?.single()
            ?.jsonObject
            ?.get("providedByConventionPlugins")
            ?.jsonArray

        usages?.map { usage ->
            val usageObject = usage.jsonObject
            Triple(
                usageObject["pluginId"]?.jsonPrimitive?.content,
                usageObject["pluginModule"]?.jsonPrimitive?.content,
                usageObject["requiredByModules"]?.jsonArray?.map { module -> module.jsonPrimitive.content }
            )
        } shouldBe listOf(
            Triple(
                "com.marmatsan.compose",
                ":gradle-plugins:compose",
                listOf(":app", ":core:ui")
            )
        )
    }

    test("generate omits included-build catalog types without roots") {
        // GIVEN
        val generator = generator()

        // WHEN
        val result = generator.generate(request())

        // THEN
        val gradlePluginsCatalog = result.model["content"]
            ?.jsonObject
            ?.get("catalogs")
            ?.jsonObject
            ?.get("gradlePlugins")
            ?.jsonObject

        gradlePluginsCatalog?.get("libraries") shouldBe null
        gradlePluginsCatalog?.get("plugins") shouldBe null
    }

    test("generate writes external topology and effective TeamCity configuration") {
        // WHEN
        val result = generator().generate(request())

        // THEN
        result.model["schemaVersion"]?.jsonPrimitive?.content shouldBe "3"
        val ci = result.model["content"]?.jsonObject?.get("ci")?.jsonObject
        ci?.get("externalTopology")?.jsonObject
            ?.get("nodes")?.jsonArray?.single()?.jsonObject
            ?.get("name")?.jsonPrimitive?.content shouldBe "Operator"
        ci?.get("teamCity")?.jsonObject
            ?.get("pipelines")?.jsonArray?.single()?.jsonObject
            ?.get("name")?.jsonPrimitive?.content shouldBe "CI"
    }
})

private fun generator(): FigmaDesignModelGenerator =
    FigmaDesignModelGenerator(
        repositoryVersionsPort = FakeRepositoryVersionsPort,
        projectCatalogTreesPort = FakeProjectCatalogTreesPort,
        projectModulesPort = FakeProjectModulesPort,
        projectModuleDependenciesPort = FakeProjectModuleDependenciesPort,
        ciExternalTopologyPort = FakeCiExternalTopologyPort,
        teamCityConfigurationPort = FakeTeamCityConfigurationPort
    )

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
        ciExternalTopologyFile = File("docs/ci/external-topology.yaml"),
        teamCityGeneratedConfigurationDirectory = File(".teamcity/target/generated-configs"),
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
    override fun readLibraryTree(source: ProjectCatalogTreeSource): LibraryCatalogTree {
        if (source is ProjectCatalogTreeSource.IncludedBuildSettings) {
            return LibraryCatalogTree(roots = emptyList())
        }

        val conventionPluginUsages = if (
            source is ProjectCatalogTreeSource.DependenciesDslVersionAliases &&
            source.conventionPluginIncludedBuilds.any { includedBuild -> includedBuild.modulePathPrefix == ":gradle-plugins" }
        ) {
            listOf(
                ConventionPluginUsage(
                    pluginId = "com.marmatsan.compose",
                    pluginModule = ":gradle-plugins:compose",
                    requiredByModules = listOf(":core:ui", ":app")
                )
            )
        } else {
            emptyList()
        }
        val conventionPluginConfigurationUsages = if (
            source is ProjectCatalogTreeSource.DependenciesDslVersionAliases &&
            source.conventionPluginIncludedBuilds.any { includedBuild -> includedBuild.modulePathPrefix == ":gradle-plugins" }
        ) {
            listOf(
                ConventionPluginConfigurationUsage(
                    pluginId = "com.marmatsan.kotlin",
                    pluginModule = ":gradle-plugins:kotlin",
                    target = "kotlin.compiler.classpath"
                )
            )
        } else {
            emptyList()
        }

        return LibraryCatalogTree(
            roots = listOf(
                LibraryCatalogNode(
                    group = "org.jetbrains.kotlin",
                    entries = listOf(
                        LibraryCatalogEntry.Artifact(
                            artifact = "kotlin-stdlib",
                            version = CatalogVersion("2.4.0"),
                            requiredByModules = listOf(":app"),
                            providedByConventionPlugins = conventionPluginUsages,
                            configuredByConventionPlugins = conventionPluginConfigurationUsages
                        )
                    )
                )
            )
        )
    }

    override fun readPluginTree(source: ProjectCatalogTreeSource): PluginCatalogTree {
        if (source is ProjectCatalogTreeSource.IncludedBuildSettings) {
            return PluginCatalogTree(roots = emptyList())
        }

        val conventionPluginUsages = if (
            source is ProjectCatalogTreeSource.DependenciesDslVersionAliases &&
            source.conventionPluginIncludedBuilds.any { includedBuild -> includedBuild.modulePathPrefix == ":gradle-plugins" }
        ) {
            listOf(
                PluginCatalogNode.ConventionPluginUsage(
                    pluginId = "com.marmatsan.compose",
                    pluginModule = ":gradle-plugins:compose",
                    requiredByModules = listOf(":core:ui", ":app")
                )
            )
        } else {
            emptyList()
        }

        return PluginCatalogTree(
            roots = listOf(
                PluginCatalogNode(
                    id = "org.jetbrains.kotlin.android",
                    version = CatalogVersion("2.4.0"),
                    appliedToModules = listOf(":app"),
                    providedByConventionPlugins = conventionPluginUsages
                )
            )
        )
    }
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

private object FakeTeamCityConfigurationPort : TeamCityConfigurationPort {
    override fun readConfiguration(source: TeamCityGeneratedConfigurationSource): TeamCityConfiguration =
        TeamCityConfiguration(
            pipelines = listOf(
                TeamCityPipeline(
                    id = "Root_Ci",
                    name = "CI",
                    triggers = emptyList(),
                    jobs = listOf(
                        TeamCityJob(
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
