package com.marmatsan.figmaDocumentationSync.domain.model.writer

/** Repository source link rendered in a managed Figma documentation header. */
data class FigmaProjectLink(
    val label: String,
    val url: String
)
