package com.marmatsan.figmaDocumentationSync.domain.model.writer

/** Figma section whose managed header links to repository sources. */
data class FigmaHeaderSectionTarget(
    val sectionNodeId: String,
    val links: List<FigmaProjectLink>
)
