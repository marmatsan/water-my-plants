package com.marmatsan.projectConfig.figma.catalog

import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogEntry.ConventionPluginConfigurationUsage
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogEntry.ConventionPluginUsage

/**
 * Library usage contributed by repository convention plugins.
 *
 * @property coordinates convention plugins providing resolved coordinates.
 * @property bundles convention plugins providing catalog bundles.
 * @property configuredCoordinates convention plugins configuring coordinates as tools.
 */
internal data class ConventionPluginLibraryUsages(
    val coordinates: Map<String, List<ConventionPluginUsage>> = emptyMap(),
    val bundles: Map<String, List<ConventionPluginUsage>> = emptyMap(),
    val configuredCoordinates: Map<String, List<ConventionPluginConfigurationUsage>> = emptyMap()
)
