package com.marmatsan.projectConfig.figma.catalog

import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogTree

/** Enriches a plugin tree without reading repository state. */
internal interface PluginCatalogUsageEnricher {
    /** Returns [tree] annotated with direct and convention-plugin usage. */
    fun enrich(
        tree: PluginCatalogTree,
        mainUsages: Map<String, Set<String>>,
        conventionPluginUsages: Map<String, List<PluginCatalogNode.ConventionPluginUsage>>
    ): PluginCatalogTree
}
