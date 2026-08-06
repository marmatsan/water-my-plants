package com.marmatsan.projectConfig.figma.catalog

/**
 * Direct product-module library usage indexed by catalog representation.
 *
 * @property coordinates modules using resolved group-artifact coordinates.
 * @property bundles modules using a catalog bundle alias.
 * @property aliases modules using a single-library catalog alias.
 */
internal data class MainLibraryUsages(
    val coordinates: Map<String, Set<String>> = emptyMap(),
    val bundles: Map<String, Set<String>> = emptyMap(),
    val aliases: Map<String, Set<String>> = emptyMap()
)
