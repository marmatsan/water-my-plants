package com.marmatsan.dependencies.catalog.api

/**
 * Immutable dependency catalog consumed across build boundaries.
 *
 * @property libraries Hierarchical Maven group roots and their catalog entries.
 * @property plugins Hierarchical Gradle plugin id roots.
 */
data class DependencyCatalog(
    val libraries: List<LibraryCatalogNode>,
    val plugins: List<PluginCatalogNode>,
)

/**
 * One segment in a hierarchical Maven group tree.
 *
 * @property group Group path value contributed by this node.
 * @property entries Artifacts and bundles registered for the complete group path at this node.
 * @property children Descendant group path nodes in declaration order.
 * @throws IllegalArgumentException if [group] is not exactly one path segment.
 */
data class LibraryCatalogNode(
    val group: String,
    val entries: List<LibraryCatalogEntry> = emptyList(),
    val children: List<LibraryCatalogNode> = emptyList(),
) {
    init {
        requireCatalogPathSegment(
            name = "Library group",
            value = group,
        )
    }
}

/** Public library entry contract, independent from the catalog-building DSL. */
sealed interface LibraryCatalogEntry {
    /**
     * One Maven artifact.
     *
     * @property name Maven artifact identifier.
     * @property version Concrete version, or `null` when externally managed.
     */
    data class Artifact(
        val name: String,
        val version: String?,
    ) : LibraryCatalogEntry

    /**
     * Multiple Maven artifacts registered under one Gradle bundle alias.
     *
     * @property alias Gradle version catalog bundle alias.
     * @property artifacts Maven artifact identifiers included in the bundle.
     * @property version Version shared by every artifact, or `null` when externally managed.
     */
    data class Bundle(
        val alias: String,
        val artifacts: List<String>,
        val version: String?,
    ) : LibraryCatalogEntry
}

/**
 * One segment in a hierarchical Gradle plugin id tree.
 *
 * @property id Plugin id path value contributed by this node.
 * @property version Concrete plugin version; `null` makes this node a namespace only.
 * @property children Descendant plugin id path nodes in declaration order.
 * @throws IllegalArgumentException if [id] is not exactly one path segment.
 */
data class PluginCatalogNode(
    val id: String,
    val version: String? = null,
    val children: List<PluginCatalogNode> = emptyList(),
) {
    init {
        requireCatalogPathSegment(
            name = "Plugin id",
            value = id,
        )
    }
}

/** Enforces the one-segment invariant for a public dependency-catalog node. */
private fun requireCatalogPathSegment(
    name: String,
    value: String,
) {
    require(
        value.isNotEmpty() &&
            '.' !in value &&
            value.none { character -> character.isWhitespace() },
    ) {
        "$name '$value' must be one non-blank path segment without dots or whitespace"
    }
}
