package com.marmatsan.verificationPlatform.domain.service.documentation

/** Parses the constrained YAML frontmatter subset owned by the documentation contract. */
internal class DocumentationFrontmatterParser {
    /**
     * Parses leading frontmatter without interpreting arbitrary YAML features.
     *
     * @param content complete Markdown source.
     * @return parsed frontmatter, or `null` when the document does not begin with a valid block.
     */
    fun parse(
        content: String
    ): DocumentationFrontmatter? {
        val match = FRONTMATTER_PATTERN.find(content) ?: return null
        val metadata = linkedMapOf<String, String>()
        val sources = mutableListOf<String>()
        var currentKey: String? = null
        match.groups["yaml"]!!.value.lineSequence().forEach { line ->
            val keyMatch = METADATA_KEY_PATTERN.matchEntire(line)
            if (keyMatch != null) {
                val key = keyMatch.groups["key"]!!.value
                currentKey = key
                metadata[key] =
                    keyMatch.groups["value"]?.value.orEmpty().trim().trim(
                        '"',
                        '\''
                    )
            } else if (currentKey == "sources") {
                SOURCE_ITEM_PATTERN
                    .matchEntire(line)
                    ?.groups
                    ?.get("value")
                    ?.value
                    ?.let { value ->
                        sources +=
                            value.trim().trim(
                                '"',
                                '\''
                            )
                    }
            }
        }
        return DocumentationFrontmatter(
            metadata = metadata,
            sources = sources,
            body = content.substring(match.range.last + 1)
        )
    }

    private companion object {
        val FRONTMATTER_PATTERN =
            Regex(
                """\A---\r?\n(?<yaml>.*?)\r?\n---(?:\r?\n|\z)""",
                RegexOption.DOT_MATCHES_ALL
            )
        val METADATA_KEY_PATTERN = Regex("""(?<key>[a-z][a-z0-9-]*):(?:\s*(?<value>.*))?""")
        val SOURCE_ITEM_PATTERN = Regex("""\s+-\s+(?<value>.+?)\s*""")
    }
}
