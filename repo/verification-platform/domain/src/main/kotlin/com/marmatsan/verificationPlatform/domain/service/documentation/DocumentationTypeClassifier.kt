package com.marmatsan.verificationPlatform.domain.service.documentation

internal class DocumentationTypeClassifier {
    val typedDocumentTypes =
        setOf(
            "standard",
            "guide",
            "runbook",
            "reference",
            "adr",
        )

    fun expectedType(
        path: String,
    ): String? =
        when {
            path == "docs/documentation.md" -> {
                "standard"
            }

            ADR_PATH_PATTERN.matches(path) -> {
                "adr"
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
        val TYPED_PATH_PATTERN = Regex("""(?:^|/)docs/(?<kind>standards|guides|reference|runbooks)/.+\.md$""")
    }
}
