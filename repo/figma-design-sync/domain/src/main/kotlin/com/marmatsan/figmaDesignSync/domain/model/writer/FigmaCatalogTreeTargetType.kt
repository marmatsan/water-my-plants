package com.marmatsan.figmaDesignSync.domain.model.writer

/** Visual node family rendered for a configured dependency catalog target. */
enum class FigmaCatalogTreeTargetType(
    val wireValue: String
) {
    LIBRARY("Library"),
    PLUGIN("Plugin")
}
