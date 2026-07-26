package com.marmatsan.dependencies.catalog.api

import java.io.File

/** Stable API implemented by a repository-owned product catalog. */
interface DependencyCatalogProvider {
    /** Returns the catalog with versions resolved from files below [rootDir]. */
    fun resolved(
        rootDir: File,
    ): DependencyCatalog

    /** Returns the catalog with stable version-property aliases. */
    fun withVersionAliases(): DependencyCatalog
}
