package com.marmatsan.dependencies.catalog.dsl

import com.marmatsan.dependencies.catalog.DependencyCatalogTrees
import com.marmatsan.dependencies.catalog.version.DependencyVersionResolver

/**
 * Builds one dependency catalog with the same tree syntax used by the Gradle settings adapter.
 *
 * @param versionResolver Strategy for concrete or symbolic version lookup.
 * @param content Library and plugin roots that form the catalog.
 * @return Immutable dependency catalog trees.
 */
fun dependencyCatalogTrees(
    versionResolver: DependencyVersionResolver,
    content: DependencyCatalogTreesBuilder.() -> Unit
): DependencyCatalogTrees =
    DependencyCatalogTreesBuilder(
        versionResolver = versionResolver
    ).apply(content).build()
