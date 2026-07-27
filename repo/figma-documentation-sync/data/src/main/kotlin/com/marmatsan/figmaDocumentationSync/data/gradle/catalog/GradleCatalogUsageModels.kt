package com.marmatsan.figmaDocumentationSync.data.gradle.catalog

/** Usage index for libraries declared as direct coordinates, bundles, or aliases. */
data class LibraryUsages(
    val coordinates: Map<String, Set<String>> = emptyMap(),
    val bundles: Map<String, Set<String>> = emptyMap(),
    val aliases: Map<String, Set<String>> = emptyMap(),
)

/** Usage index for dependency notations assigned to convention-plugin configuration targets. */
data class LibraryConfigurationUsages(
    val coordinates: Map<String, Set<LibraryConfigurationUsage>> = emptyMap(),
)

/** One convention-plugin configuration assignment of a library coordinate. */
data class LibraryConfigurationUsage(
    val pluginModule: String,
    val target: String,
) : Comparable<LibraryConfigurationUsage> {
    override fun compareTo(
        other: LibraryConfigurationUsage,
    ): Int =
        compareValuesBy(
            this,
            other,
            LibraryConfigurationUsage::pluginModule,
            LibraryConfigurationUsage::target,
        )
}

internal operator fun LibraryUsages.plus(
    other: LibraryUsages,
): LibraryUsages =
    LibraryUsages(
        coordinates = coordinates.mergeUsageSets(other.coordinates),
        bundles = bundles.mergeUsageSets(other.bundles),
        aliases = aliases.mergeUsageSets(other.aliases),
    )

internal operator fun LibraryConfigurationUsages.plus(
    other: LibraryConfigurationUsages,
): LibraryConfigurationUsages =
    LibraryConfigurationUsages(
        coordinates = coordinates.mergeUsageSets(other.coordinates),
    )

internal fun <T : Comparable<T>> Map<String, Set<T>>.mergeUsageSets(
    other: Map<String, Set<T>>,
): Map<String, Set<T>> =
    (keys + other.keys).associateWith { key ->
        (this[key].orEmpty() + other[key].orEmpty()).toSortedSet()
    }
