package com.marmatsan.verificationPlatform.domain.service.documentation

/** Consumer-owned extension point for one independent typed-document validation rule. */
internal fun interface TypedDocumentationRule {
    /** Adds this rule's errors or warnings for [context] to [findings]. */
    fun validate(
        context: TypedDocumentationContext,
        findings: DocumentationFindings
    )
}
