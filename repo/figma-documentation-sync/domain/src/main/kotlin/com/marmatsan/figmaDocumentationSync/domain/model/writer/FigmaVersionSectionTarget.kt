package com.marmatsan.figmaDocumentationSync.domain.model.writer

/** Parent section and variable folder used by one dependency version group. */
data class FigmaVersionSectionTarget(
    val parentNodeId: String,
    val variableFolder: String
)
