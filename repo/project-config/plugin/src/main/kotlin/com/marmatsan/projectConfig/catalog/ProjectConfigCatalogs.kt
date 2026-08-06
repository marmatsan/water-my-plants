package com.marmatsan.projectConfig.catalog

import com.marmatsan.dependencies.catalog.api.DependencyCatalog
import com.marmatsan.dependencies.catalog.api.VersionAliasedDependencyCatalogProvider

/** Public project boundary for the consumer-owned dependency catalog definition. */
class ProjectConfigCatalogs internal constructor(
    private val provider: VersionAliasedDependencyCatalogProvider
) {
    /** Returns the configured library and plugin trees with stable version-property aliases. */
    fun withVersionAliases(): DependencyCatalog = provider.withVersionAliases()
}
