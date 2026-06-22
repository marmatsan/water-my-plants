package com.marmatsan.figmaDesignSync.plugin.generator

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
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.time.Instant
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

    test("generate changes the model hash when gitSha changes") {
        // GIVEN
        val generator = generator()

        // WHEN
        val first = generator.generate(request(gitSha = "abc123"))
        val second = generator.generate(request(gitSha = "def456"))

        // THEN
        first.modelHash shouldBe first.model["modelHash"]?.jsonPrimitive?.content
        second.modelHash shouldBe second.model["modelHash"]?.jsonPrimitive?.content
        (first.modelHash == second.modelHash) shouldBe false
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
            ?.toList() shouldBe listOf("androidGradlePlugin", "kotlinVersion")
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
        } shouldBe listOf("Main project dependencies", "Libraries")
    }
})

private fun generator(): FigmaDesignModelGenerator =
    FigmaDesignModelGenerator(
        repositoryVersionsPort = FakeRepositoryVersionsPort,
        projectCatalogTreesPort = FakeProjectCatalogTreesPort,
        projectModulesPort = FakeProjectModulesPort,
        projectModuleDependenciesPort = FakeProjectModuleDependenciesPort
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
        buildLogicSettingsFile = File("build-logic/settings.gradle.kts"),
        projectRootDirectory = File("."),
        buildLogicRootDirectory = File("build-logic")
    )

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
