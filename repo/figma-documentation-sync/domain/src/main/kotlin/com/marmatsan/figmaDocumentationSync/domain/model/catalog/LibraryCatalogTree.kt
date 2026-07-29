package com.marmatsan.figmaDocumentationSync.domain.model.catalog

/**
 * Root container for the library dependency catalog tree rendered into Figma.
 *
 * The tree is part of the generated `design-model.json` contract and is later
 * consumed by the Figma sync tooling to draw dependency documentation.
 *
 * Example:
 * ```
 * LibraryCatalogTree(
 *     roots = listOf(LibraryCatalogNode(group = "org"))
 * )
 * ```
 *
 * @property roots Top-level group-id nodes.
 */
data class LibraryCatalogTree(
    val roots: List<LibraryCatalogNode>
)
