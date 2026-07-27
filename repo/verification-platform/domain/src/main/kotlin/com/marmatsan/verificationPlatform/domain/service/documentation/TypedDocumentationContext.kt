package com.marmatsan.verificationPlatform.domain.service.documentation

import java.time.LocalDate

internal data class TypedDocumentationContext(
    val path: String,
    val expectedType: String,
    val frontmatter: DocumentationFrontmatter,
    val repositoryEntries: Set<String>,
    val currentDate: LocalDate,
)
