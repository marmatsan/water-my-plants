package com.marmatsan.verificationPlatform.domain.service

/** Validates repository-local Markdown links without applying provider-specific exceptions. */
internal class DocumentationLinkValidator(
    private val paths: DocumentationPathResolver = DocumentationPathResolver(),
) {
    fun validate(
        documentPath: String,
        content: String,
        repositoryEntries: Set<String>,
        findings: DocumentationFindings,
    ) {
        MARKDOWN_LINK_PATTERN.findAll(content).forEach { match ->
            val target =
                match.groups["target"]!!.value.trim(
                    '<',
                    '>',
                )
            if (EXTERNAL_LINK_PATTERN.containsMatchIn(target)) return@forEach
            val targetPath =
                target
                    .split(
                        '#',
                        '?',
                        limit = 2,
                    ).first()
            if (targetPath.isBlank()) return@forEach
            val resolved =
                paths.resolve(
                    documentPath,
                    targetPath,
                )
            if (resolved == null || paths.normalize(resolved) !in repositoryEntries) {
                findings.errors += "[$documentPath] Broken local Markdown link: $target"
            }
        }
    }

    private companion object {
        val MARKDOWN_LINK_PATTERN = Regex("""(?<!!)\[[^]]*]\((?<target>[^ )]+)""")
        val EXTERNAL_LINK_PATTERN = Regex("^(https?:|mailto:|#)")
    }
}
