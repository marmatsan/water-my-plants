package com.marmatsan.figmaDocumentationSync.data.gradle.catalog

/** Usage index for dependency notations assigned to convention-plugin configuration targets. */
data class LibraryConfigurationUsages(
    val coordinates: Map<String, Set<LibraryConfigurationUsage>> = emptyMap(),
)

internal operator fun LibraryConfigurationUsages.plus(
    other: LibraryConfigurationUsages,
): LibraryConfigurationUsages =
    LibraryConfigurationUsages(
        coordinates = coordinates.mergeUsageSets(other.coordinates),
    )
