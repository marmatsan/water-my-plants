package com.marmatsan.verificationPlatform.domain.service

import java.time.LocalDate

internal data class DocumentationFrontmatter(
    val metadata: Map<String, String>,
    val sources: List<String>,
    val body: String,
)

internal data class TypedDocumentationContext(
    val path: String,
    val expectedType: String,
    val frontmatter: DocumentationFrontmatter,
    val repositoryEntries: Set<String>,
    val currentDate: LocalDate,
)

internal class DocumentationFindings(
    val errors: MutableList<String> = mutableListOf(),
    val warnings: MutableList<String> = mutableListOf(),
)

internal fun interface TypedDocumentationRule {
    fun validate(
        context: TypedDocumentationContext,
        findings: DocumentationFindings,
    )
}

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

internal class DocumentationFrontmatterParser {
    fun parse(
        content: String,
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
                        '\'',
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
                                '\'',
                            )
                    }
            }
        }
        return DocumentationFrontmatter(
            metadata = metadata,
            sources = sources,
            body = content.substring(match.range.last + 1),
        )
    }

    private companion object {
        val FRONTMATTER_PATTERN =
            Regex(
                """\A---\r?\n(?<yaml>.*?)\r?\n---(?:\r?\n|\z)""",
                RegexOption.DOT_MATCHES_ALL,
            )
        val METADATA_KEY_PATTERN = Regex("""(?<key>[a-z][a-z0-9-]*):(?:\s*(?<value>.*))?""")
        val SOURCE_ITEM_PATTERN = Regex("""\s+-\s+(?<value>.+?)\s*""")
    }
}

internal class DocumentationPathResolver {
    fun normalize(
        path: String,
    ): String =
        path
            .trim()
            .replace(
                '\\',
                '/',
            ).trimStart(
                '.',
                '/',
            )

    fun sourceExists(
        source: String,
        repositoryEntries: Set<String>,
    ): Boolean {
        val normalized =
            normalize(
                path = source,
            )
        return if (normalized.any { character -> character == '*' || character == '?' }) {
            repositoryEntries.any { entry ->
                wildcardMatches(
                    path = entry,
                    pattern = normalized,
                )
            }
        } else {
            normalized in repositoryEntries
        }
    }

    fun matchesAny(
        path: String,
        patterns: List<String>,
    ): Boolean =
        patterns.any { pattern ->
            wildcardMatches(
                path = path,
                pattern =
                    normalize(
                        path = pattern,
                    ),
            )
        }

    fun resolve(
        documentPath: String,
        targetPath: String,
    ): String? {
        val parts =
            if (targetPath.startsWith('/')) {
                targetPath.trimStart('/').split('/')
            } else {
                documentPath
                    .substringBeforeLast(
                        '/',
                        "",
                    ).split('/')
                    .filter(String::isNotEmpty) +
                    targetPath.split('/')
            }
        val resolved = mutableListOf<String>()
        parts.forEach { part ->
            when (part) {
                "", "." -> Unit
                ".." -> if (resolved.isEmpty()) return null else resolved.removeLast()
                else -> resolved += part
            }
        }
        return resolved.joinToString("/")
    }

    private fun wildcardMatches(
        path: String,
        pattern: String,
    ): Boolean {
        val regex =
            buildString {
                append('^')
                pattern.forEach { character ->
                    when (character) {
                        '*' -> append(".*")
                        '?' -> append('.')
                        else -> append(Regex.escape(character.toString()))
                    }
                }
                append('$')
            }
        return Regex(
            regex,
            RegexOption.IGNORE_CASE,
        ).matches(
            normalize(
                path = path,
            ),
        )
    }
}
