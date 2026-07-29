package com.marmatsan.dependencies.catalog.dsl

import com.marmatsan.dependencies.catalog.DependencyCatalogTrees
import com.marmatsan.dependencies.catalog.version.DependencyVersionResolver

/**
 * Builds one immutable library-and-plugin catalog from the shared tree DSL.
 *
 * The builder owns declaration collection and catalog-level invariants. Version representation is
 * supplied through [versionResolver], allowing the same declaration to produce a resolved Gradle
 * catalog or a symbolic documentation catalog.
 *
 * @property versionResolver Strategy used by [version] for every declared version key.
 */
class DependencyCatalogTreesBuilder(
    private val versionResolver: DependencyVersionResolver
) {
    private val libraryTrees = LibraryCatalogTreesScope()
    private val pluginTrees = PluginCatalogTreesScope()
    private var built = false

    /**
     * Adds Maven group trees to this catalog.
     *
     * Multiple calls accumulate declarations until [build] is called.
     *
     * @param content Library roots and their relative group paths.
     */
    fun libraries(
        content: LibraryCatalogTreesScope.() -> Unit
    ) {
        checkMutable()
        libraryTrees.content()
    }

    /**
     * Adds Gradle plugin id trees to this catalog.
     *
     * Multiple calls accumulate declarations until [build] is called.
     *
     * @param content Plugin roots and their relative id paths.
     */
    fun plugins(
        content: PluginCatalogTreesScope.() -> Unit
    ) {
        checkMutable()
        pluginTrees.content()
    }

    /**
     * Resolves one exact, case-sensitive dependency version [key].
     *
     * @return Concrete or symbolic version text chosen by the configured resolver.
     */
    fun version(
        key: String
    ): String {
        checkMutable()
        return versionResolver.resolve(
            key = key
        )
    }

    /**
     * Returns the immutable catalog represented by all collected declarations.
     *
     * A builder is single-use. At least one library or plugin root must exist.
     *
     * @throws IllegalArgumentException when no root was declared.
     * @throws IllegalStateException when this builder was already built.
     */
    fun build(): DependencyCatalogTrees {
        checkMutable()
        val trees =
            DependencyCatalogTrees(
                libraries = libraryTrees.values(),
                plugins = pluginTrees.values()
            )
        require(trees.libraries.isNotEmpty() || trees.plugins.isNotEmpty()) {
            "Tree dependency catalog must declare at least one library or plugin root"
        }
        built = true
        return trees
    }

    private fun checkMutable() {
        check(!built) {
            "Dependency catalog trees builder cannot be reused after build()"
        }
    }
}
