package com.marmatsan.figmaDocumentationSync.domain.model.writer

/**
 * Repository source link rendered in a managed Figma documentation header.
 *
 * @property label text rendered for the repository source.
 * @property url canonical main-branch URL opened from the label.
 */
data class FigmaProjectLink(
    val label: String,
    val url: String
)
