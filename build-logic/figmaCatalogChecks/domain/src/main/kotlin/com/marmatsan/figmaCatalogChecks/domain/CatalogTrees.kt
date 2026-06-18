package com.marmatsan.figmaCatalogChecks.domain

data class CatalogVersion(
    val value: String?,
    val visible: Boolean = value != null
) {
    init {
        require(value != null || !visible) {
            "A catalog version cannot be visible when its value is null"
        }
    }
}

data class LibraryCatalogTree(
    val roots: List<LibraryCatalogNode>
)

data class LibraryCatalogNode(
    val group: String,
    val entries: List<LibraryCatalogEntry> = emptyList(),
    val artifactsVisible: Boolean = entries.isNotEmpty(),
    val children: List<LibraryCatalogNode> = emptyList()
)

sealed interface LibraryCatalogEntry {
    data class Artifact(
        val artifact: String,
        val version: CatalogVersion,
        val requiredByModules: List<String> = emptyList()
    ) : LibraryCatalogEntry

    data class ArtifactsBundle(
        val alias: String,
        val artifacts: List<String>,
        val version: CatalogVersion,
        val requiredByModules: List<String> = emptyList()
    ) : LibraryCatalogEntry
}

data class PluginCatalogTree(
    val roots: List<PluginCatalogNode>
)

data class PluginCatalogNode(
    val id: String,
    val version: CatalogVersion? = null,
    val appliedToModules: List<String> = emptyList(),
    val children: List<PluginCatalogNode> = emptyList()
)
