package com.marmatsan.verificationPlatform.domain.service

import com.marmatsan.verificationPlatform.domain.model.DocumentationCoverageRule
import com.marmatsan.verificationPlatform.domain.model.DocumentationCoverageViolation

/** Validates that changed implementation paths include their required documentation surfaces. */
internal class DocumentationCoverageValidator(
    private val paths: DocumentationPathResolver = DocumentationPathResolver(),
) {
    fun validate(
        changedPaths: List<String>,
        rules: List<DocumentationCoverageRule>,
    ): List<DocumentationCoverageViolation> {
        val normalizedPaths =
            changedPaths
                .map(paths::normalize)
                .filter(String::isNotBlank)
                .distinct()
        return rules.mapNotNull { rule ->
            val changedSources =
                normalizedPaths.filter { path ->
                    paths.matchesAny(
                        path,
                        rule.sourcePaths,
                    )
                }
            if (changedSources.isEmpty()) return@mapNotNull null
            val documentationChanged =
                normalizedPaths.any { path ->
                    paths.matchesAny(
                        path,
                        rule.documentationPaths,
                    )
                }
            if (documentationChanged) return@mapNotNull null
            DocumentationCoverageViolation(
                rule = rule.id,
                changedSources = changedSources,
                requiredDocumentation = rule.documentationPaths,
            )
        }
    }
}
