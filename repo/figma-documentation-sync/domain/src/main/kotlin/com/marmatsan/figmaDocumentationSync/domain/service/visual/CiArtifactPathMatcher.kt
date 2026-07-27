package com.marmatsan.figmaDocumentationSync.domain.service.visual

/** Compares TeamCity artifact rules with required portable artifact paths. */
internal class CiArtifactPathMatcher {
    fun contains(
        publishedPath: String,
        requiredFile: String,
    ): Boolean {
        val normalizedPublishedPath =
            normalize(
                path = publishedPath,
            )
        val normalizedRequiredFile =
            normalize(
                path = requiredFile,
            )
        return normalizedPublishedPath == normalizedRequiredFile ||
            normalizedRequiredFile.startsWith("$normalizedPublishedPath/")
    }

    private fun normalize(
        path: String,
    ): String =
        path
            .substringBefore("=>")
            .trim()
            .replace(
                '\\',
                '/',
            ).replace(
                Regex("/(?:\\*\\*?|\\*\\.\\*)$"),
                "",
            ).removeSuffix("/")
}
