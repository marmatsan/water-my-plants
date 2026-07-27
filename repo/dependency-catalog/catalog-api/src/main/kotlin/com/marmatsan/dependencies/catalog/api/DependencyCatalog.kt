package com.marmatsan.dependencies.catalog.api

/** Immutable dependency catalog consumed across build boundaries. */
data class DependencyCatalog(
    val libraries: List<LibraryCatalogNode>,
    val plugins: List<PluginCatalogNode>,
)

/** One segment in a hierarchical Maven group tree. */
data class LibraryCatalogNode(
    val group: String,
    val entries: List<LibraryCatalogEntry> = emptyList(),
    val children: List<LibraryCatalogNode> = emptyList(),
)

/** Public library entry contract, independent from the catalog-building DSL. */
sealed interface LibraryCatalogEntry {
    /** One Maven artifact. */
    data class Artifact(
        val name: String,
        val version: String?,
    ) : LibraryCatalogEntry

    /** Multiple Maven artifacts registered under one Gradle bundle alias. */
    data class Bundle(
        val alias: String,
        val artifacts: List<String>,
        val version: String?,
    ) : LibraryCatalogEntry
}

/** One segment in a hierarchical Gradle plugin id tree. */
data class PluginCatalogNode(
    val id: String,
    val version: String? = null,
    val children: List<PluginCatalogNode> = emptyList(),
)
