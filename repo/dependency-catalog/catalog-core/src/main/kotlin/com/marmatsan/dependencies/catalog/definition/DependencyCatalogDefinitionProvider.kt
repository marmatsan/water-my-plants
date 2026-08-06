package com.marmatsan.dependencies.catalog.definition

import com.marmatsan.dependencies.catalog.api.DependencyCatalog
import com.marmatsan.dependencies.catalog.api.DependencyCatalogProvider
import com.marmatsan.dependencies.catalog.version.DependencyVersionAliasResolver
import com.marmatsan.dependencies.catalog.version.PropertiesDependencyVersionResolver
import java.io.File

/**
 * Exposes resolved and version-aliased views of one [DependencyCatalogDefinition].
 *
 * @param definition Single source of truth for the catalog structure.
 * @param versionsFile Resolves the consumer-owned version registry from its root directory.
 */
class DependencyCatalogDefinitionProvider(
    private val definition: DependencyCatalogDefinition,
    private val versionsFile: (File) -> File = { rootDirectory ->
        rootDirectory.resolve("versions.properties")
    }
) : DependencyCatalogProvider {
    /** Resolves every version key from the registry selected for [rootDir]. */
    override fun resolved(
        rootDir: File
    ): DependencyCatalog =
        definition.catalog(
            versionResolver =
                PropertiesDependencyVersionResolver(
                    source = {
                        versionsFile(rootDir)
                    }
                )
        )

    /** Materializes the same catalog with stable property keys as version values. */
    override fun withVersionAliases(): DependencyCatalog =
        definition.catalog(
            versionResolver = DependencyVersionAliasResolver
        )
}
