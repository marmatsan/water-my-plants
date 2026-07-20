package com.marmatsan.figmaDocumentationSync.domain.samples

import com.marmatsan.figmaDocumentationSync.domain.model.catalog.CatalogVersion
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogEntry
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogEntry.ConventionPluginUsage
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogNode
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogTree
import com.marmatsan.figmaDocumentationSync.domain.model.modules.ModuleDependency
import com.marmatsan.figmaDocumentationSync.domain.model.versions.RepositoryVersionSection
import com.marmatsan.figmaDocumentationSync.domain.port.catalog.ProjectCatalogTreeSource
import com.marmatsan.figmaDocumentationSync.domain.port.catalog.ProjectCatalogTreesPort
import com.marmatsan.figmaDocumentationSync.domain.port.gradle.IncludedBuildSource
import com.marmatsan.figmaDocumentationSync.domain.port.modules.ProjectModuleDependenciesPort
import com.marmatsan.figmaDocumentationSync.domain.port.modules.ProjectModuleDependenciesScope
import com.marmatsan.figmaDocumentationSync.domain.port.modules.ProjectModuleDependenciesSource
import com.marmatsan.figmaDocumentationSync.domain.port.modules.ProjectModulesPort
import com.marmatsan.figmaDocumentationSync.domain.port.modules.ProjectModulesSource
import com.marmatsan.figmaDocumentationSync.domain.port.versions.RepositoryVersionsPort
import com.marmatsan.figmaDocumentationSync.domain.port.versions.VersionsFileSource

/**
 * Compile-checked examples referenced by KDoc `@sample` tags.
 *
 * These samples live in `src/main` so Kotlin and Dokka can resolve them from
 * production KDoc while keeping the sample object internal to the domain
 * module.
 */
internal object DomainKDocSamples {
    fun catalogVersionSample() {
        val visibleVersion =
            CatalogVersion(
                value = "2.2.0",
            )
        val hiddenVersion =
            CatalogVersion(
                value = null,
                visible = false,
            )

        check(visibleVersion.visible)
        check(!hiddenVersion.visible)
    }

    fun libraryCatalogEntrySample() {
        val artifact =
            LibraryCatalogEntry.Artifact(
                artifact = "kotlin-stdlib",
                version =
                    CatalogVersion(
                        value = "2.4.0",
                    ),
                requiredByModules = listOf(":app"),
                providedByConventionPlugins =
                    listOf(
                        ConventionPluginUsage(
                            pluginId = "com.marmatsan.compose",
                            pluginModule = ":gradle-plugins:compose",
                            requiredByModules = listOf(":app"),
                        ),
                    ),
            )

        val bundle =
            LibraryCatalogEntry.ArtifactsBundle(
                alias = "composeBundle",
                artifacts =
                    listOf(
                        "ui",
                        "ui-graphics",
                        "ui-tooling",
                    ),
                version =
                    CatalogVersion(
                        value = "2026.05.01",
                    ),
                requiredByModules = listOf(":core:ui"),
            )

        check(artifact.requiredByModules == listOf(":app"))
        check(artifact.providedByConventionPlugins.single().pluginModule == ":gradle-plugins:compose")
        check(bundle.artifacts.contains("ui-tooling"))
    }

    fun moduleDependencySample() {
        val dependency =
            ModuleDependency(
                dependentModule = ":app",
                dependencyModule = ":core:ui",
            )

        check(dependency.render() == ":app -> :core:ui")
    }

    fun repositoryVersionSectionSample() {
        val section =
            RepositoryVersionSection(
                name = "Main project dependencies",
                versions = mapOf("kotlinVersion" to "2.4.0"),
            )

        check(section.versions["kotlinVersion"] == "2.4.0")
    }

    fun repositoryVersionsPortSample() {
        val port =
            object : RepositoryVersionsPort {
                override fun readVersions(
                    source: VersionsFileSource,
                ): Map<String, String> =
                    mapOf("kotlinVersion" to "2.4.0")

                override fun readVersionSections(
                    source: VersionsFileSource,
                ): List<RepositoryVersionSection> =
                    listOf(
                        RepositoryVersionSection(
                            name = "Main project dependencies",
                            versions =
                                readVersions(
                                    source = source,
                                ),
                        ),
                    )
            }

        val source =
            VersionsFileSource(
                path = "repo/dependency-catalog/versions.properties",
            )
        val versions =
            port.readVersions(
                source = source,
            )
        val sections =
            port.readVersionSections(
                source = source,
            )

        check(versions["kotlinVersion"] == "2.4.0")
        check(sections.single().name == "Main project dependencies")
    }

    fun projectCatalogTreesPortSample() {
        val port =
            object : ProjectCatalogTreesPort {
                override fun readLibraryTree(
                    source: ProjectCatalogTreeSource,
                ): LibraryCatalogTree =
                    LibraryCatalogTree(
                        roots =
                            listOf(
                                LibraryCatalogNode(
                                    group = "org",
                                    children =
                                        listOf(
                                            LibraryCatalogNode(
                                                group = "jetbrains",
                                            ),
                                        ),
                                ),
                            ),
                    )

                override fun readPluginTree(
                    source: ProjectCatalogTreeSource,
                ): PluginCatalogTree =
                    PluginCatalogTree(
                        roots =
                            listOf(
                                PluginCatalogNode(
                                    id = "org",
                                    children =
                                        listOf(
                                            PluginCatalogNode(
                                                id = "jetbrains",
                                            ),
                                        ),
                                ),
                            ),
                    )
            }

        val includedBuild =
            IncludedBuildSource(
                settingsFilePath = "repo/gradle-plugins/settings.gradle.kts",
                rootDirPath = "repo/gradle-plugins",
                modulePathPrefix = ":gradle-plugins",
            )
        val source =
            ProjectCatalogTreeSource.IncludedBuildSettings(
                includedBuild = includedBuild,
            )
        val libraryTree = port.readLibraryTree(source)
        val pluginTree = port.readPluginTree(source)

        check(libraryTree.roots.single().group == "org")
        check(pluginTree.roots.single().id == "org")
    }

    fun projectModulesPortSample() {
        val port =
            object : ProjectModulesPort {
                override fun readModules(
                    source: ProjectModulesSource,
                ): Set<String> =
                    setOf(
                        ":app",
                        ":core:ui",
                        ":gradle-plugins:dependencies",
                    )
            }

        val source =
            ProjectModulesSource(
                rootSettingsFilePath = "settings.gradle.kts",
                includedBuilds =
                    listOf(
                        IncludedBuildSource(
                            settingsFilePath = "repo/gradle-plugins/settings.gradle.kts",
                            rootDirPath = "repo/gradle-plugins",
                            modulePathPrefix = ":gradle-plugins",
                        ),
                    ),
            )
        val modules = port.readModules(source)

        check(":gradle-plugins:dependencies" in modules)
    }

    fun projectModuleDependenciesPortSample() {
        val port =
            object : ProjectModuleDependenciesPort {
                override fun readModuleDependencies(
                    source: ProjectModuleDependenciesSource,
                ): Set<ModuleDependency> =
                    setOf(
                        ModuleDependency(
                            dependentModule = ":app",
                            dependencyModule = ":core:ui",
                        ),
                    )
            }

        val source =
            ProjectModuleDependenciesSource(
                rootDirPath = "repo/gradle-plugins",
                scope = ProjectModuleDependenciesScope.IncludedBuild,
                modulePathPrefix = ":gradle-plugins",
            )
        val dependencies = port.readModuleDependencies(source)

        check(
            ModuleDependency(
                dependentModule = ":app",
                dependencyModule = ":core:ui",
            ) in dependencies,
        )
    }
}
