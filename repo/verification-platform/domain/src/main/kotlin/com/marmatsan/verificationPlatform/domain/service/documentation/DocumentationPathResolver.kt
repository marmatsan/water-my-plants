package com.marmatsan.verificationPlatform.domain.service.documentation

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
