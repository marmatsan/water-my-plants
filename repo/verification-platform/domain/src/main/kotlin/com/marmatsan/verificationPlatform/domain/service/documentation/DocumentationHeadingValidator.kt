package com.marmatsan.verificationPlatform.domain.service.documentation

/** Validates required Markdown headings for each typed-document contract. */
internal class DocumentationHeadingValidator : TypedDocumentationRule {
    /** Validates the title and type-specific second-level headings required by the document contract. */
    override fun validate(
        context: TypedDocumentationContext,
        findings: DocumentationFindings,
    ) {
        val body = context.frontmatter.body
        if (!LEVEL_ONE_HEADING_PATTERN.containsMatchIn(body)) {
            findings.errors += "[${context.path}] A level-one title is required after frontmatter."
        }
        val headings =
            LEVEL_TWO_HEADING_PATTERN
                .findAll(body)
                .map { match ->
                    match.groups["name"]!!
                        .value
                        .trim()
                        .lowercase()
                }.toSet()
        if (context.expectedType == "runbook") {
            RUNBOOK_SECTIONS.forEach { (name, aliases) ->
                if (headings.none(aliases::contains)) {
                    findings.errors += "[${context.path}] Runbook section '$name' is required."
                }
            }
        }
        if (context.expectedType == "adr") {
            ADR_SECTIONS.forEach { heading ->
                if (heading !in headings) {
                    findings.errors += "[${context.path}] ADR section '$heading' is required."
                }
            }
        }
    }

    private companion object {
        val LEVEL_ONE_HEADING_PATTERN =
            Regex(
                """^#\s+\S""",
                RegexOption.MULTILINE,
            )
        val LEVEL_TWO_HEADING_PATTERN =
            Regex(
                """^##\s+(?<name>.+?)\s*$""",
                RegexOption.MULTILINE,
            )
        val RUNBOOK_SECTIONS =
            listOf(
                "Purpose" to setOf("purpose"),
                "Prerequisites" to setOf("prerequisites"),
                "Verification" to
                    setOf(
                        "verification",
                        "verify",
                    ),
                "Recovery" to
                    setOf(
                        "recovery",
                        "failure recovery",
                    ),
                "Prohibited Actions" to setOf("prohibited actions"),
                "Sources" to setOf("sources"),
            )
        val ADR_SECTIONS =
            setOf(
                "context",
                "decision",
                "consequences",
                "alternatives",
                "supersession",
            )
    }
}
