package com.marmatsan.dependencies.gradle

import org.gradle.api.artifacts.ExternalModuleDependencyBundle
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.provider.Provider

/**
 * Returns the dependency notation registered for a required library alias.
 *
 * This is intended for gradle-plugins plugins that need a plain dependency notation string, for example when passing a
 * catalog dependency to custom helper functions or Gradle APIs that do not accept a version catalog provider directly.
 *
 * @param alias Version catalog alias to resolve, such as `androidx.compose.bom`.
 * @return Dependency notation produced by Gradle for the alias, such as `androidx.compose:compose-bom:2025.06.01`.
 * @throws NoSuchElementException When [alias] is not present in this version catalog.
 */
fun VersionCatalog.requireDependencyNotation(
    alias: String
): String = findLibrary(alias)
    .orElseThrow {
        NoSuchElementException("Library alias '$alias' not found in version catalog named ${this.name}")
    }
    .get()
    .toString()

/**
 * Returns the provider registered for a required library bundle alias.
 *
 * This is intended for gradle-plugins plugins that need to add all dependencies declared in a catalog bundle.
 *
 * @param alias Version catalog bundle alias to resolve, such as `composeBundle`.
 * @return Provider for the dependency bundle registered for [alias].
 * @throws NoSuchElementException When [alias] is not present in this version catalog.
 */
fun VersionCatalog.requireBundle(
    alias: String
): Provider<ExternalModuleDependencyBundle> = findBundle(alias)
    .orElseThrow {
        NoSuchElementException("Bundle alias '$alias' not found in version catalog named ${this.name}")
    }

/**
 * Returns the dependency notation for a Maven coordinate registered in this version catalog.
 *
 * The alias is derived with [libraryAlias], using the same rule as catalog registration. This allows callers to request
 * a dependency by its real Maven group and artifact while still resolving the generated catalog alias.
 *
 * Example:
 *
 * ```
 * requireDependencyNotation(
 *     libraryGroup = "androidx.compose",
 *     artifact = "compose-bom"
 * )
 * ```
 *
 * resolves the alias `androidx.compose.bom`.
 *
 * @param libraryGroup Maven group identifier, such as `androidx.compose`.
 * @param artifact Maven artifact identifier, such as `compose-bom`.
 * @return Dependency notation produced by Gradle for the generated alias.
 * @throws NoSuchElementException When the generated alias is not present in this version catalog.
 * @see libraryAlias
 */
fun VersionCatalog.requireDependencyNotation(
    libraryGroup: String,
    artifact: String
): String = requireDependencyNotation(
    alias = libraryAlias(
        libraryGroup = libraryGroup,
        artifact = artifact
    )
)
