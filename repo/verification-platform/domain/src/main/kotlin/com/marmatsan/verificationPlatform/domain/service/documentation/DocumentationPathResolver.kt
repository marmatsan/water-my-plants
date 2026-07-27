package com.marmatsan.verificationPlatform.domain.service.documentation

/** Resolves and matches repository-relative documentation paths without filesystem access. */
internal class DocumentationPathResolver {
    /** Returns a slash-separated repository path without leading relative markers. */
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

    /** Returns whether a literal or wildcard source declaration matches a repository entry. */
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

    /** Returns whether [path] matches at least one normalized wildcard pattern. */
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

    /**
     * Resolves a Markdown target relative to its containing document.
     *
     * @return the repository-relative target, or `null` when traversal escapes the repository root.
     */
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
