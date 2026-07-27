package com.marmatsan.verificationPlatform.domain.service.documentation

internal fun interface TypedDocumentationRule {
    fun validate(
        context: TypedDocumentationContext,
        findings: DocumentationFindings,
    )
}
