package com.marmatsan.figmaDesignSync.data.dependencies.catalog

import com.marmatsan.dependencies.catalog.DependencyCatalogTrees
import java.io.File

/**
 * Project adapter that supplies the repository-owned dependency catalog DSL.
 *
 * The portable sync engine depends on this interface instead of a concrete
 * application catalog. Projects provide an implementation from their
 * `project-config` adapter.
 */
interface DependencyCatalogProvider {
    /** Returns catalog trees with concrete versions resolved from [rootDir]. */
    fun resolved(rootDir: File): DependencyCatalogTrees

    /** Returns catalog trees whose versions contain stable repository aliases. */
    fun withVersionAliases(): DependencyCatalogTrees
}
