package com.marmatsan.figmaDocumentationSync.domain.model.writer

/**
 * Figma section whose managed header links to repository sources.
 *
 * @property sectionNodeId Figma section containing the managed header.
 * @property links ordered repository links rendered in the header.
 * @property definition optional repository-owned definition rendered in the header.
 */
data class FigmaHeaderSectionTarget(
    val sectionNodeId: String,
    val links: List<FigmaProjectLink>,
    val definition: String? = null,
)
