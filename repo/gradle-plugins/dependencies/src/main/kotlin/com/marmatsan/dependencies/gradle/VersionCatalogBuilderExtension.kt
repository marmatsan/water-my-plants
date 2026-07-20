package com.marmatsan.dependencies.gradle

import com.marmatsan.dependencies.tree.model.Artifact
import com.marmatsan.dependencies.tree.model.ArtifactsBundle
import com.marmatsan.dependencies.tree.model.Dependency
import com.marmatsan.dependencies.tree.model.LibraryEntry
import org.gradle.api.initialization.dsl.VersionCatalogBuilder

/**
 * Registers all library dependencies in this [VersionCatalogBuilder].
 *
 * Each [Dependency.Library] contributes a Maven group path and optional catalog entries. Libraries with `null` entries
 * are skipped because they represent structural tree nodes only.
 *
 * - [LibraryEntry.Single] registers one library alias for its artifact.
 * - [LibraryEntry.Bundle] registers every artifact in the bundle and then creates a Gradle version catalog bundle from
 * the generated aliases.
 *
 * Alias generation is handled by [libraryAlias], so repeated group/artifact prefixes are collapsed. For example,
 * `androidx.compose:compose-bom` is registered as `androidx.compose.bom`, not `androidx.compose.compose.bom`.
 *
 * @param libraries Flattened library dependencies, usually produced by a dependency tree traversal.
 */
fun VersionCatalogBuilder.registerLibraries(
    libraries: List<Dependency.Library>
) {
    libraries.forEach { library ->
        library.entries?.forEach { entry ->
            registerLibraryEntry(
                libraryGroup = library.libraryGroup,
                entry = entry
            )
        }
    }
}

/**
 * Registers all versioned Gradle plugins in this [VersionCatalogBuilder].
 *
 * Each [Dependency.Plugin] is already a final registrable plugin, so its full plugin id is used as both the catalog
 * alias and the Gradle plugin id.
 *
 * @param plugins Flattened plugin dependencies, usually produced by a dependency tree traversal.
 */
fun VersionCatalogBuilder.registerPlugins(
    plugins: List<Dependency.Plugin>
) {
    plugins.forEach { plugin ->
        plugin(
            plugin.pluginId,
            plugin.pluginId
        ).version(plugin.version)
    }
}

/**
 * Registers one library catalog entry for a Maven group.
 *
 * Single entries are registered directly, while bundle entries are delegated to [registerLibraryBundle].
 *
 * @param libraryGroup Maven group for the entry.
 * @param entry Catalog entry declared under [libraryGroup].
 */
private fun VersionCatalogBuilder.registerLibraryEntry(
    libraryGroup: String,
    entry: LibraryEntry
) {
    when (entry) {
        is LibraryEntry.Single -> registerLibrary(
            libraryGroup = libraryGroup,
            artifact = entry.artifact
        )

        is LibraryEntry.Bundle -> registerLibraryBundle(
            libraryGroup = libraryGroup,
            bundle = entry.artifactsBundle
        )
    }
}

/**
 * Registers all artifacts in [bundle] and creates a Gradle version catalog bundle from their generated aliases.
 *
 * When [ArtifactsBundle.version] is not `null`, the same version is applied to every artifact registered for the
 * bundle. When it is `null`, each artifact is registered without a version, allowing a BOM or another external
 * constraint to provide it.
 *
 * @param libraryGroup Maven group shared by the bundled artifacts.
 * @param bundle Bundle declaration to register.
 */
private fun VersionCatalogBuilder.registerLibraryBundle(
    libraryGroup: String,
    bundle: ArtifactsBundle
) {
    val aliases = bundle.artifacts.map { artifact ->
        registerLibrary(
            libraryGroup = libraryGroup,
            artifact = artifact,
            version = bundle.version
        )
    }

    bundle(
        bundle.alias,
        aliases
    )
}

/**
 * Registers a library alias in the current [VersionCatalogBuilder].
 *
 * This sets up an alias in the version catalog that maps to the specified `<libraryGroup>:<artifact name>`, but does
 * not yet define a version.
 *
 * Use the returned [VersionCatalogBuilder.LibraryAliasBuilder] to configure a version using [registerLibraryVersion].
 *
 * @param libraryAlias The alias name used to reference the dependency in build scripts (e.g., `androidx.core.ktx`)
 * @param libraryGroup The group ID of the dependency (e.g., `androidx.core`)
 * @param artifact The artifact ID of the dependency (e.g., `core-ktx`)
 * @return A [VersionCatalogBuilder.LibraryAliasBuilder] to allow setting or omitting the version
 * @see [registerLibraryVersion]
 */
private fun VersionCatalogBuilder.registerLibraryAlias(
    libraryAlias: String,
    libraryGroup: String,
    artifact: String
) = library(
    libraryAlias,
    libraryGroup,
    artifact
)

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
 * A library is defined using the format `<libraryGroup>:<artifact>:<version>`.
 * - If [version] is provided, it is assigned to use the specified version
 * - If [version] is `null`, the library is registered without a version
 *
 * This function generates an alias from the full library group and the artifact name, removing the longest artifact
 * prefix already represented by the group path. Hyphens in the remaining artifact segment are normalized to dots.
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
 *  * **Alias**: `androidx.core.ktx`
 *  * **Usage**: `<version catalog name>.<alias>`
 *
 * @param libraryGroup The group ID of the library (e.g., `androidx.core`)
 * @param artifact The [Artifact] to register
 * @param version The optional version for the artifact. If `null`, the version is expected to be managed elsewhere
 * (e.g., by a BOM)
 * @return The generated alias for the registered library.
 * @see [registerLibraryAlias]
 * @see [registerLibraryVersion]
 */
private fun VersionCatalogBuilder.registerLibrary(
    libraryGroup: String,
    artifact: Artifact,
    version: String? = artifact.version
): String {
    val libraryAlias = libraryAlias(
        libraryGroup = libraryGroup,
        artifact = artifact.artifact
    )
    val libraryAliasBuilder = registerLibraryAlias(
        libraryAlias = libraryAlias,
        libraryGroup = libraryGroup,
        artifact = artifact.artifact
    )
    libraryAliasBuilder.registerLibraryVersion(version)
    return libraryAlias
}

/**
 * Builds the version catalog alias for a Maven coordinate.
 *
 * The alias starts with [libraryGroup]. If the [artifact] starts with a suffix already represented by the group path,
 * that overlapping prefix is removed before appending the remaining artifact segment. Hyphens in the appended segment
 * are normalized to dots.
 *
 * Examples:
 *
 * - `androidx.compose` + `compose-bom` -> `androidx.compose.bom`
 * - `androidx.activity` + `activity-compose` -> `androidx.activity.compose`
 * - `org.junit.jupiter` + `junit-jupiter-api` -> `org.junit.jupiter.api`
 * - `com.google.protobuf` + `protoc` -> `com.google.protobuf.protoc`
 *
 * @param libraryGroup Maven group identifier.
 * @param artifact Maven artifact identifier.
 * @return Version catalog alias used to register and resolve the library.
 */
internal fun libraryAlias(
    libraryGroup: String,
    artifact: String
): String {
    val groupSegments = libraryGroup.split(".")
    var groupSuffix = ""
    var artifactAliasSegment: String? = null

    for (index in groupSegments.lastIndex downTo 0) {
        groupSuffix = if (groupSuffix.isEmpty()) {
            groupSegments[index]
        } else {
            "${groupSegments[index]}-$groupSuffix"
        }

        artifactAliasSegment = when {
            artifact == groupSuffix -> ""
            artifact.startsWith("$groupSuffix-") -> artifact.removePrefix("$groupSuffix-")
            else -> null
        }

        if (artifactAliasSegment != null) {
            break
        }
    }

    val normalizedArtifactAliasSegment = (artifactAliasSegment ?: artifact).replace(
        "-",
        "."
    )

    return if (artifactAliasSegment?.isEmpty() == true) {
        libraryGroup
    } else {
        "$libraryGroup.$normalizedArtifactAliasSegment"
    }
}
