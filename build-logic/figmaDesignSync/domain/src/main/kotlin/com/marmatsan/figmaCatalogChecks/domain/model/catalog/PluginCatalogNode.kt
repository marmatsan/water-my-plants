package com.marmatsan.figmaDesignSync.domain.model.catalog

data class PluginCatalogNode(
    val id: String,
    val version: CatalogVersion? = null,
    val appliedToModules: List<String> = emptyList(),
    val children: List<PluginCatalogNode> = emptyList()
)
