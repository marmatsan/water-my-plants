package com.marmatsan.dependencies.catalog.definition

import com.marmatsan.dependencies.catalog.api.DependencyCatalog
import com.marmatsan.dependencies.catalog.dsl.DependencyCatalogTreesBuilder
import com.marmatsan.dependencies.catalog.dsl.dependencyCatalogTrees
import com.marmatsan.dependencies.catalog.mapping.toDependencyCatalog
import com.marmatsan.dependencies.catalog.version.DependencyVersionResolver

/**
 * Reusable declaration of one library-and-plugin catalog tree.
 *
 * The declaration stores structure rather than resolved versions. Consumers can therefore
 * materialize the same roots, artifacts, bundles, and plugins with different
 * [DependencyVersionResolver] strategies without duplicating the catalog definition.
 */
class DependencyCatalogDefinition internal constructor(
    private val content: DependencyCatalogTreesBuilder.() -> Unit
) {
    /**
     * Materializes this definition using [versionResolver] and maps it to the public catalog API.
     *
     * @param versionResolver Strategy used for every `version("key")` call in the declaration.
     * @return Immutable catalog whose versions use the selected representation.
     */
    fun catalog(
        versionResolver: DependencyVersionResolver
    ): DependencyCatalog =
        dependencyCatalogTrees(
            versionResolver = versionResolver,
            content = content
        ).toDependencyCatalog()
}

/**
 * Captures one catalog declaration that can be materialized with multiple version strategies.
 *
 * @param content Library and plugin roots expressed with the canonical tree DSL.
 * @return Reusable immutable catalog definition.
 */
fun dependencyCatalogDefinition(
    content: DependencyCatalogTreesBuilder.() -> Unit
): DependencyCatalogDefinition =
    DependencyCatalogDefinition(
        content = content
    )
