package com.marmatsan.figmaDocumentationSync.domain.model.figma

/**
 * Provider-neutral Figma node content required by documentation synchronization.
 *
 * @property sharedPluginData shared plugin values keyed by namespace and key.
 */
data class FigmaNodeContent(
    val sharedPluginData: Map<String, Map<String, String>>
)
