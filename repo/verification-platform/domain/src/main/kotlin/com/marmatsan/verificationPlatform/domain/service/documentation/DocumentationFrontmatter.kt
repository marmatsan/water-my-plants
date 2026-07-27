package com.marmatsan.verificationPlatform.domain.service.documentation

/**
 * Parsed documentation frontmatter and the Markdown body that follows it.
 *
 * @property metadata scalar YAML metadata keyed by canonical field name.
 * @property sources canonical source entries declared by the document.
 * @property body Markdown content after the closing frontmatter delimiter.
 */
internal data class DocumentationFrontmatter(
    val metadata: Map<String, String>,
    val sources: List<String>,
    val body: String,
)
