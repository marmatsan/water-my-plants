package com.marmatsan.figmaCatalogChecks.domain.model.catalog

data class LibraryCatalogNode(
    val group: String,
    val entries: List<LibraryCatalogEntry> = emptyList(),
    val artifactsVisible: Boolean = entries.isNotEmpty(),
    val children: List<LibraryCatalogNode> = emptyList()
)
