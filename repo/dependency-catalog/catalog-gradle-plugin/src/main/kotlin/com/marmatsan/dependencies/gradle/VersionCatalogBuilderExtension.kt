package com.marmatsan.dependencies.gradle

import com.marmatsan.dependencies.catalog.api.LibraryCatalogEntry
import org.gradle.api.initialization.dsl.VersionCatalogBuilder
import com.marmatsan.dependencies.catalog.api.libraryAlias as catalogLibraryAlias

/**
 * Registers all library dependencies in this [VersionCatalogBuilder].
 *
 * Each resolved library contributes a Maven group path and public catalog entries.
 *
 * Artifact entries register one alias. Bundle entries register every artifact
 * and then create a Gradle version catalog bundle from the generated aliases.
 *
 * Alias generation is handled by [libraryAlias], so repeated group/artifact prefixes are collapsed. For example,
 * `androidx.compose:compose-bom` is registered as `androidx.compose.bom`, not `androidx.compose.compose.bom`.
 *
 * @param libraries Flattened library dependencies, usually produced by a dependency tree traversal.
 */
internal fun VersionCatalogBuilder.registerLibraries(
    libraries: List<ResolvedLibrary>
) {
    libraries.forEach { library ->
        library.entries.forEach { entry ->
            registerLibraryEntry(
                libraryGroup = library.group,
                entry = entry
            )
        }
    }
}

/**
 * Registers all versioned Gradle plugins in this [VersionCatalogBuilder].
 *
 * Each resolved plugin uses its full id as both the catalog alias and the
 * Gradle plugin id.
 *
 * @param plugins Flattened plugin dependencies, usually produced by a dependency tree traversal.
 */
internal fun VersionCatalogBuilder.registerPlugins(
    plugins: List<ResolvedPlugin>
) {
    plugins.forEach { plugin ->
        plugin(
            plugin.id,
            plugin.id
        ).version(
            plugin.version
        )
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
    entry: LibraryCatalogEntry
) {
    when (entry) {
        is LibraryCatalogEntry.Artifact -> {
            registerLibrary(
                libraryGroup = libraryGroup,
                artifact = entry.name,
                version = entry.version
            )
        }

        is LibraryCatalogEntry.Bundle -> {
            registerLibraryBundle(
                libraryGroup = libraryGroup,
                bundle = entry
            )
        }
    }
}

/**
 * Registers all artifacts in [bundle] and creates a Gradle version catalog bundle from their generated aliases.
 *
 * When the bundle version is not `null`, the same version is applied to every artifact registered for the
 * bundle. When it is `null`, each artifact is registered without a version, allowing a BOM or another external
 * constraint to provide it.
 *
 * @param libraryGroup Maven group shared by the bundled artifacts.
 * @param bundle Bundle declaration to register.
 */
private fun VersionCatalogBuilder.registerLibraryBundle(
    libraryGroup: String,
    bundle: LibraryCatalogEntry.Bundle
) {
    val aliases =
        bundle.artifacts.map { artifact ->
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
) = if (version == null) {
    withoutVersion()
} else {
    version(
        version
    )
}

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
 *     artifact = "core-ktx",
 *     version = "1.13.1",
 * )
 * ```
 *
 *  * **Resolved dependency**: `androidx.core:core-ktx:1.13.1`
 *  * **Alias**: `androidx.core.ktx`
 *  * **Usage**: `<version catalog name>.<alias>`
 *
 * @param libraryGroup The group ID of the library (e.g., `androidx.core`)
 * @param artifact The Maven artifact name to register.
 * @param version The optional version for the artifact. If `null`, the version is expected to be managed elsewhere
 * (e.g., by a BOM)
 * @return The generated alias for the registered library.
 * @see [registerLibraryAlias]
 * @see [registerLibraryVersion]
 */
private fun VersionCatalogBuilder.registerLibrary(
    libraryGroup: String,
    artifact: String,
    version: String?
): String {
    val libraryAlias =
        catalogLibraryAlias(
            libraryGroup = libraryGroup,
            artifact = artifact
        )
    val libraryAliasBuilder =
        registerLibraryAlias(
            libraryAlias = libraryAlias,
            libraryGroup = libraryGroup,
            artifact = artifact
        )
    libraryAliasBuilder.registerLibraryVersion(
        version = version
    )
    return libraryAlias
}

/**
 * Flattened library group ready for Gradle catalog registration.
 *
 * @property group Complete Maven group.
 * @property entries Entries registered below [group].
 */
internal data class ResolvedLibrary(
    val group: String,
    val entries: List<LibraryCatalogEntry>
)

/**
 * Flattened plugin ready for Gradle catalog registration.
 *
 * @property id Complete Gradle plugin id and catalog alias.
 * @property version Concrete plugin version.
 */
internal data class ResolvedPlugin(
    val id: String,
    val version: String
)

/**
 * Builds the stable version-catalog alias for a Maven coordinate.
 *
 * @param libraryGroup Maven group identifier.
 * @param artifact Maven artifact identifier.
 * @return Stable version-catalog alias for the coordinate.
 * @see com.marmatsan.dependencies.catalog.api.libraryAlias
 */
@Deprecated(
    message = "Import libraryAlias from catalog-api",
    replaceWith =
        ReplaceWith(
            expression = "libraryAlias(libraryGroup, artifact)",
            imports = ["com.marmatsan.dependencies.catalog.api.libraryAlias"]
        )
)
fun libraryAlias(
    libraryGroup: String,
    artifact: String
): String =
    catalogLibraryAlias(
        libraryGroup = libraryGroup,
        artifact = artifact
    )
