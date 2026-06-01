package com.marmatsan.dependencies.gradle

import com.marmatsan.dependencies.tree.model.Artifact
import com.marmatsan.dependencies.tree.model.Dependency
import com.marmatsan.dependencies.tree.model.LibraryEntry
import org.gradle.api.initialization.dsl.VersionCatalogBuilder

/**
 * TODO
 */
fun VersionCatalogBuilder.registerLibraries(
    libraries: List<Dependency.Library>
) {
    libraries.forEach { library ->
        library.entries?.forEach { entry ->
            when (entry) {
                is LibraryEntry.Single -> {
                    val artifact = entry.artifact
                    registerLibrary(
                        libraryGroup = library.libraryGroup,
                        artifact = artifact
                    )
                }

                is LibraryEntry.Bundle -> {
                    val artifactsBundle = entry.artifactsBundle
                    val artifacts = artifactsBundle.artifacts
                    val artifactsBundleVersion = artifactsBundle.version
                    val bundleAlias = artifactsBundle.alias

                    val aliases = mutableListOf<String>()

                    artifacts.forEach { artifact ->
                        aliases.add(
                            registerLibrary(
                                libraryGroup = library.libraryGroup,
                                artifact = artifact,
                                version = artifactsBundleVersion
                            )
                        )
                    }

                    bundle(bundleAlias, aliases)
                }
            }
        }
    }
}

fun VersionCatalogBuilder.registerPlugins(
    plugins: List<Dependency.Plugin>
) {
    plugins.forEach { plugin ->
        plugin.version?.let { version ->
            plugin(plugin.pluginId, plugin.pluginId).version(version)
        }
    }
}

/**
 * Registers a library alias in the current [VersionCatalogBuilder].
 *
 * This sets up an alias in the version catalog that maps to the specified `<libraryGroup>:<artifact name>`, but does
 * not yet define a version.
 *
 * Use the returned [VersionCatalogBuilder.LibraryAliasBuilder] to configure a version using [registerLibraryVersion].
 *
 * @param libraryAlias The unnormalized or normalized alias name used to reference the dependency in build scripts (e.g.
 * , `androidx.core.core.ktx`)
 * @param libraryGroup The group ID of the dependency (e.g., `androidx.core`)
 * @param artifact The artifact ID of the dependency (e.g., `core-ktx`)
 * @return A [VersionCatalogBuilder.LibraryAliasBuilder] to allow setting or omitting the version
 * @see [registerLibraryVersion]
 */
private fun VersionCatalogBuilder.registerLibraryAlias(
    libraryAlias: String,
    libraryGroup: String,
    artifact: String
) = library(libraryAlias, libraryGroup, artifact)

/**
 * Registers the given [version] for this library alias.
 *
 * - If [version] is non-null, it assigns the version to the alias using [version]
 * - If [version] is null, it marks the library as versionless, assuming the version will be managed externally
 * (e.g., by a BOM)
 *
 * @receiver The [VersionCatalogBuilder.LibraryAliasBuilder] representing the alias to configure
 * @param version The optional version string to assign. If `null`, the library is declared without a version
 */
private fun VersionCatalogBuilder.LibraryAliasBuilder.registerLibraryVersion(
    version: String? = null
) = if (version == null) withoutVersion() else version(version)


/**
 * Registers a library dependency in the current [VersionCatalogBuilder].
 *
 * A library is defined using the format: `library group.artifact name:artifact version`.
 * - If [version] is provided, it is assigned to use the specified version
 * - If [version] is `null`, the library is registered without a version
 *
 * This function generates an alias in the format `<library group>.<artifact name>`, which can be used in build scripts
 * via `<version catalog name>.<normalized alias>`. The normalized alias is generated from the generated alias,
 * replacing `-` with `.`
 *
 * Example
 *
 * ```
 * registerLibrary(
 *     libraryGroup = "androidx.core",
 *     artifact = Artifact(artifact = "core-ktx", version = "1.13.1")
 * )
 * ```
 *
 *  * **Resolved dependency**: `androidx.core:core-ktx:1.13.1`
 *  * **Unnormalized alias**: `androidx.core.core-ktx` **Normalized alias**: `androidx.core.core.ktx`
 *  * **Usage**: `<version catalog name>.<normalized alias>`
 *
 * @param libraryGroup The group ID of the library (e.g., `androidx.core`)
 * @param artifact The [Artifact] to register
 * @param version The optional version for the artifact. If `null`, the version is expected to be managed elsewhere
 * (e.g., by a BOM)
 * @return The generated **unnormalized** alias for the registered library in form of a [String]
 * @see [registerLibraryAlias]
 * @see [registerLibraryVersion]
 */
private fun VersionCatalogBuilder.registerLibrary(
    libraryGroup: String,
    artifact: Artifact,
    version: String? = artifact.version
): String {
    val libraryAlias = "$libraryGroup.${artifact.artifact}"
    val libraryAliasBuilder = registerLibraryAlias(
        libraryAlias = libraryAlias,
        libraryGroup = libraryGroup,
        artifact = artifact.artifact
    )
    libraryAliasBuilder.registerLibraryVersion(version)
    return libraryAlias
}
