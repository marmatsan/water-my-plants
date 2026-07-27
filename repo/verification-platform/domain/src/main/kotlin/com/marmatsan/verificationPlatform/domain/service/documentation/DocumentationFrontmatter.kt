package com.marmatsan.verificationPlatform.domain.service.documentation

internal data class DocumentationFrontmatter(
    val metadata: Map<String, String>,
    val sources: List<String>,
    val body: String,
)
