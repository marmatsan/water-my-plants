package com.marmatsan.dependencies.data

import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.initialization.dsl.VersionCatalogBuilder
import org.gradle.api.initialization.resolve.DependencyResolutionManagement
import org.gradle.kotlin.dsl.DependencyHandlerScope

fun VersionCatalog.getLibrary(libraryAlias: String): String {
    return findLibrary(libraryAlias).get().get().toString()
}

fun DependencyHandlerScope.implementation(dependencyNotation: String): org.gradle.api.artifacts.Dependency? =
    add("implementation", dependencyNotation)

fun DependencyHandlerScope.implementation(dependency: org.gradle.api.artifacts.Dependency): org.gradle.api.artifacts.Dependency? =
    add("implementation", dependency)

fun DependencyHandlerScope.testImplementation(dependencyNotation: String): org.gradle.api.artifacts.Dependency? =
    add("testImplementation", dependencyNotation)

fun DependencyHandlerScope.ksp(dependencyNotation: String): org.gradle.api.artifacts.Dependency? =
    add("ksp", dependencyNotation)

fun DependencyResolutionManagement.buildVersionCatalogs() {
    versionCatalogs {
        create("libs") {
            nonComposeLibraryTrees.forEach {
                createLibraries(it.getLibraries())
            }
        }
        create("libsCompose") {
            composeLibraryTrees.forEach {
                createLibraries(it.getLibraries())
            }
        }
        create("plugins") {
            pluginTrees.forEach {
                createPlugins(it.getPlugins())
            }
        }
    }
}

private fun VersionCatalogBuilder.createLibraries(libraries: List<Dependency.Library>) {
    libraries.forEach { library ->
        val libraryGroup = library.group
        val artifactsGroups = library.artifactsGroups

        if (artifactsGroups?.size == 1 && artifactsGroups.first().artifacts.size == 1) {
            val libraryArtifact = artifactsGroups.first().artifacts.first()
            val libraryVersion = artifactsGroups.first().version
            library(
                libraryGroup,
                "$libraryGroup:$libraryArtifact:$libraryVersion"
            )
        } else {
            artifactsGroups?.forEach { artifactsGroup ->
                val bundleName = "$libraryGroup.${artifactsGroup.name}"
                version(
                    bundleName,
                    artifactsGroup.version
                )
                val artifactsAliases = mutableListOf<String>()
                artifactsGroup.artifacts.forEach { artifact ->
                    val artifactAlias = "$libraryGroup.$artifact"
                    library(
                        artifactAlias,
                        libraryGroup,
                        artifact
                    ).versionRef(bundleName)
                    artifactsAliases.add(artifactAlias)
                }
                bundle(
                    bundleName,
                    artifactsAliases
                )
            }
        }
    }
}

private fun VersionCatalogBuilder.createPlugins(plugins: List<Dependency.Plugin>) {
    plugins.forEach { plugin ->
        if (plugin.version != null) // TODO: Plugin version is always not null here
            plugin(
                plugin.id,
                plugin.id
            ).version(plugin.version)
    }
}