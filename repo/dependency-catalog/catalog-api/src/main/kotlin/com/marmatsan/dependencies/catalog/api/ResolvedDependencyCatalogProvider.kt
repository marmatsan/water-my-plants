package com.marmatsan.dependencies.catalog.api

import java.io.File

/** Supplies dependency catalogs whose versions are resolved for a concrete repository checkout. */
interface ResolvedDependencyCatalogProvider {
    /** Returns the catalog with versions resolved from files below [rootDir]. */
    fun resolved(
        rootDir: File,
    ): DependencyCatalog
}
