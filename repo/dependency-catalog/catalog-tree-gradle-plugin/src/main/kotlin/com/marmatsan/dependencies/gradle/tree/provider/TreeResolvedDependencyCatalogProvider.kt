package com.marmatsan.dependencies.gradle.tree.provider

import com.marmatsan.dependencies.catalog.api.DependencyCatalog
import com.marmatsan.dependencies.catalog.api.ResolvedDependencyCatalogProvider
import java.io.File

/** Supplies the catalog assembled by the settings tree DSL to the provider-based adapter. */
internal class TreeResolvedDependencyCatalogProvider(
    private val catalog: DependencyCatalog,
) : ResolvedDependencyCatalogProvider {
    /**
     * Returns the settings-assembled [catalog].
     *
     * [rootDir] is unused because the tree DSL has already resolved all consumer-owned versions.
     *
     * @param rootDir Consumer root required by the shared provider contract.
     * @return Immutable resolved catalog assembled during settings evaluation.
     */
    override fun resolved(
        rootDir: File,
    ): DependencyCatalog = catalog
}
