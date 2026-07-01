package com.marmatsan.figmaDesignSync.domain.samples

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
import com.marmatsan.figmaDesignSync.domain.port.modules.ProjectModuleDependenciesScope
import com.marmatsan.figmaDesignSync.domain.port.modules.ProjectModuleDependenciesSource
import com.marmatsan.figmaDesignSync.domain.port.modules.ProjectModulesPort
import com.marmatsan.figmaDesignSync.domain.port.modules.ProjectModulesSource
import com.marmatsan.figmaDesignSync.domain.port.versions.RepositoryVersionsPort
import com.marmatsan.figmaDesignSync.domain.port.versions.VersionsFileSource

/**
 * Compile-checked examples referenced by KDoc `@sample` tags.
 *
 * These samples live in `src/main` so Kotlin and Dokka can resolve them from
 * production KDoc while keeping the sample object internal to the domain
 * module.
 */
internal object DomainKDocSamples {
    fun catalogVersionSample() {
        val visibleVersion = CatalogVersion(value = "2.2.0")
        val hiddenVersion = CatalogVersion(value = null, visible = false)

        check(visibleVersion.visible)
        check(!hiddenVersion.visible)
    }

    fun libraryCatalogEntrySample() {
        val artifact = LibraryCatalogEntry.Artifact(
            artifact = "kotlin-stdlib",
            version = CatalogVersion("2.4.0"),
            requiredByModules = listOf(":app")
        )

        val bundle = LibraryCatalogEntry.ArtifactsBundle(
            alias = "composeBundle",
            artifacts = listOf("ui", "ui-graphics", "ui-tooling"),
            version = CatalogVersion("2026.05.01"),
            requiredByModules = listOf(":core:ui")
        )

        check(artifact.requiredByModules == listOf(":app"))
        check(bundle.artifacts.contains("ui-tooling"))
    }

    fun moduleDependencySample() {
        val dependency = ModuleDependency(
            dependentModule = ":app",
            dependencyModule = ":core:ui"
        )

        check(dependency.render() == ":app -> :core:ui")
    }

    fun repositoryVersionSectionSample() {
        val section = RepositoryVersionSection(
            name = "Main project dependencies",
            versions = mapOf("kotlinVersion" to "2.4.0")
        )

        check(section.versions["kotlinVersion"] == "2.4.0")
    }

    fun repositoryVersionsPortSample() {
        val port = object : RepositoryVersionsPort {
            override fun readVersions(source: VersionsFileSource): Map<String, String> =
                mapOf("kotlinVersion" to "2.4.0")

            override fun readVersionSections(source: VersionsFileSource): List<RepositoryVersionSection> =
                listOf(
                    RepositoryVersionSection(
                        name = "Main project dependencies",
                        versions = readVersions(source)
                    )
                )
        }

        val source = VersionsFileSource("build-logic/versions.properties")
        val versions = port.readVersions(source)
        val sections = port.readVersionSections(source)

        check(versions["kotlinVersion"] == "2.4.0")
        check(sections.single().name == "Main project dependencies")
    }

    fun projectCatalogTreesPortSample() {
        val port = object : ProjectCatalogTreesPort {
            override fun readLibraryTree(source: ProjectCatalogTreeSource): LibraryCatalogTree =
                LibraryCatalogTree(
                    roots = listOf(
                        LibraryCatalogNode(
                            group = "org",
                            children = listOf(LibraryCatalogNode(group = "jetbrains"))
                        )
                    )
                )

            override fun readPluginTree(source: ProjectCatalogTreeSource): PluginCatalogTree =
                PluginCatalogTree(
                    roots = listOf(
                        PluginCatalogNode(
                            id = "org",
                            children = listOf(PluginCatalogNode(id = "jetbrains"))
                        )
                    )
                )
        }

        val source = ProjectCatalogTreeSource.BuildLogicSettings(
            settingsFilePath = "build-logic/settings.gradle.kts"
        )
        val libraryTree = port.readLibraryTree(source)
        val pluginTree = port.readPluginTree(source)

        check(libraryTree.roots.single().group == "org")
        check(pluginTree.roots.single().id == "org")
    }

    fun projectModulesPortSample() {
        val port = object : ProjectModulesPort {
            override fun readModules(source: ProjectModulesSource): Set<String> =
                setOf(":app", ":core:ui", ":figmaDesignSync:domain")
        }

        val source = ProjectModulesSource(
            rootSettingsFilePath = "settings.gradle.kts",
            buildLogicSettingsFilePath = "build-logic/settings.gradle.kts"
        )
        val modules = port.readModules(source)

        check(":figmaDesignSync:domain" in modules)
    }

    fun projectModuleDependenciesPortSample() {
        val port = object : ProjectModuleDependenciesPort {
            override fun readModuleDependencies(
                source: ProjectModuleDependenciesSource
            ): Set<ModuleDependency> =
                setOf(
                    ModuleDependency(
                        dependentModule = ":app",
                        dependencyModule = ":core:ui"
                    )
                )
        }

        val source = ProjectModuleDependenciesSource(
            rootDirPath = ".",
            scope = ProjectModuleDependenciesScope.Main
        )
        val dependencies = port.readModuleDependencies(source)

        check(ModuleDependency(":app", ":core:ui") in dependencies)
    }
}
