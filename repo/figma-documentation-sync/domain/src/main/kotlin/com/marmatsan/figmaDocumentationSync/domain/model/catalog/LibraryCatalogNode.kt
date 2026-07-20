package com.marmatsan.figmaDocumentationSync.domain.model.catalog

/**
 * Group node in the library dependency catalog tree rendered in Figma.
 *
 * The [group] usually represents one segment of a Maven group id. [entries]
 * hold artifacts or bundles that live at this node, while [children] model the
 * nested package-like hierarchy that the Figma dependency tree displays.
 *
 * Example:
 * ```
 * LibraryCatalogNode(
 *     group = "org",
 *     children = listOf(LibraryCatalogNode(group = "jetbrains"))
 * )
 * ```
 *
 * @property group Group-id segment represented by this node.
 * @property entries Library artifacts or bundles attached directly to this
 * node.
 * @property artifactsVisible Whether Figma should expand the artifact row for
 * this node.
 * @property children Nested group-id segments.
 */
data class LibraryCatalogNode(
    val group: String,
    val entries: List<LibraryCatalogEntry> = emptyList(),
    val artifactsVisible: Boolean = entries.isNotEmpty(),
    val children: List<LibraryCatalogNode> = emptyList(),
)
