package com.marmatsan.figmaDocumentationSync.data.gradle.catalog

/**
 * Usage index for libraries declared as direct coordinates, bundles, or aliases.
 *
 * @property coordinates consuming modules keyed by Maven coordinate.
 * @property bundles consuming modules keyed by bundle alias.
 * @property aliases consuming modules keyed by library alias.
 */
data class LibraryUsages(
    val coordinates: Map<String, Set<String>> = emptyMap(),
    val bundles: Map<String, Set<String>> = emptyMap(),
    val aliases: Map<String, Set<String>> = emptyMap()
)

internal operator fun LibraryUsages.plus(
    other: LibraryUsages
): LibraryUsages =
    LibraryUsages(
        coordinates = coordinates.mergeUsageSets(other.coordinates),
        bundles = bundles.mergeUsageSets(other.bundles),
        aliases = aliases.mergeUsageSets(other.aliases)
    )

internal fun <T : Comparable<T>> Map<String, Set<T>>.mergeUsageSets(
    other: Map<String, Set<T>>
): Map<String, Set<T>> =
    (keys + other.keys).associateWith { key ->
        (this[key].orEmpty() + other[key].orEmpty()).toSortedSet()
    }
