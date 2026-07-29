package com.marmatsan.verificationPlatform.domain.service.documentation

import java.time.LocalDate

/**
 * Immutable input shared by every rule that validates one typed document.
 *
 * @property path normalized repository-relative document path.
 * @property expectedType document type implied by canonical placement.
 * @property frontmatter parsed metadata, sources, and Markdown body.
 * @property repositoryEntries normalized paths available for source validation.
 * @property currentDate deterministic date used to evaluate review windows.
 */
internal data class TypedDocumentationContext(
    val path: String,
    val expectedType: String,
    val frontmatter: DocumentationFrontmatter,
    val repositoryEntries: Set<String>,
    val currentDate: LocalDate
)
