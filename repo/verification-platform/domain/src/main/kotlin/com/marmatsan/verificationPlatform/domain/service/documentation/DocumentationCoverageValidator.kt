package com.marmatsan.verificationPlatform.domain.service.documentation

import com.marmatsan.verificationPlatform.domain.model.documentation.DocumentationCoverageRule
import com.marmatsan.verificationPlatform.domain.model.documentation.DocumentationCoverageViolation

/** Validates that changed implementation paths include their required documentation surfaces. */
internal class DocumentationCoverageValidator(
    private val paths: DocumentationPathResolver = DocumentationPathResolver(),
) {
    /**
     * Finds changed source groups whose configured documentation surface was not changed.
     *
     * @param changedPaths repository-relative paths included in the change.
     * @param rules source-to-documentation coverage rules to evaluate.
     * @return one violation for every matched rule without a documentation change.
     */
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
