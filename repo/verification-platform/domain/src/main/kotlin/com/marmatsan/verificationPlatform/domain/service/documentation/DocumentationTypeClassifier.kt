package com.marmatsan.verificationPlatform.domain.service.documentation

/** Maps canonical documentation paths to the typed-document contract they must satisfy. */
internal class DocumentationTypeClassifier {
    /** Document type values that require canonical placement and metadata validation. */
    val typedDocumentTypes =
        setOf(
            "standard",
            "guide",
            "runbook",
            "reference",
            "adr",
            "specification"
        )

    /** Returns the required document type for [path], or `null` for untyped documentation. */
    fun expectedType(
        path: String
    ): String? =
        when {
            path == "docs/documentation.md" -> {
                "standard"
            }

            ADR_PATH_PATTERN.matches(path) -> {
                "adr"
            }

            SPECIFICATION_PATH_PATTERN.matches(path) -> {
                "specification"
            }

            path.endsWith("/README.md") -> {
                null
            }

            TYPED_PATH_PATTERN.containsMatchIn(path) -> {
                when (TYPED_PATH_PATTERN.find(path)!!.groups["kind"]!!.value) {
                    "standards" -> "standard"
                    "guides" -> "guide"
                    "reference" -> "reference"
                    "runbooks" -> "runbook"
                    else -> null
                }
            }

            else -> {
                null
            }
        }

    private companion object {
        val ADR_PATH_PATTERN = Regex("""docs/decisions/adr-[0-9]{4}-[a-z0-9]+(?:-[a-z0-9]+)*\.md""")
        val SPECIFICATION_PATH_PATTERN =
            Regex(
                """specs/[0-9]{3}-[a-z0-9]+(?:-[a-z0-9]+)*/(?:spec|plan|checklist)\.md"""
            )
        val TYPED_PATH_PATTERN = Regex("""(?:^|/)docs/(?<kind>standards|guides|reference|runbooks)/.+\.md$""")
    }
}
