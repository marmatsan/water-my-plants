package com.marmatsan.dependencies.gradle.tree.dsl

/**
 * Compatibility alias for the catalog-core library tree scope.
 *
 * New provider implementations should import
 * [com.marmatsan.dependencies.catalog.dsl.LibraryCatalogTreesScope] directly.
 */
@Deprecated(
    message = "Use com.marmatsan.dependencies.catalog.dsl.LibraryCatalogTreesScope",
    replaceWith = ReplaceWith("com.marmatsan.dependencies.catalog.dsl.LibraryCatalogTreesScope")
)
typealias LibraryCatalogTreesScope =
    com.marmatsan.dependencies.catalog.dsl.LibraryCatalogTreesScope
