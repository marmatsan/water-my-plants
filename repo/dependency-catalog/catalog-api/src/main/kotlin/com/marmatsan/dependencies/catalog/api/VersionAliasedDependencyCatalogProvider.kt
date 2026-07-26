package com.marmatsan.dependencies.catalog.api

/** Supplies dependency catalogs whose versions retain stable property aliases. */
interface VersionAliasedDependencyCatalogProvider {
    /** Returns the catalog with stable version-property aliases. */
    fun withVersionAliases(): DependencyCatalog
}
