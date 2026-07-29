package com.marmatsan.figmaDocumentationSync.domain.model.writer

/**
 * Visual node family rendered for a configured dependency catalog target.
 *
 * @property wireValue serialized component-family value consumed by the writer.
 */
enum class FigmaCatalogTreeTargetType(
    val wireValue: String
) {
    LIBRARY("Library"),
    PLUGIN("Plugin")
}
