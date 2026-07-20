package com.marmatsan.verificationPlatform.domain.model

/**
 * One Markdown document discovered inside a repository checkout.
 *
 * @property path normalized repository-relative path.
 * @property content complete Markdown source.
 */
data class DocumentationFile(
    val path: String,
    val content: String
)

/**
 * Provider-neutral filesystem snapshot consumed by documentation validation.
 *
 * @property documents Markdown sources eligible for validation.
 * @property repositoryEntries normalized file and directory paths used to
 * resolve canonical sources and local links.
 */
data class DocumentationRepositorySnapshot(
    val documents: List<DocumentationFile>,
    val repositoryEntries: Set<String>
)

/**
 * One implementation-to-documentation coverage rule.
 *
 * @property id stable rule identifier reported on failure.
 * @property sourcePaths wildcard patterns that activate the rule.
 * @property documentationPaths wildcard patterns that satisfy the rule.
 */
data class DocumentationCoverageRule(
    val id: String,
    val sourcePaths: List<String>,
    val documentationPaths: List<String>
)

/**
 * Missing documentation update for an activated coverage rule.
 *
 * @property rule id of the activated rule.
 * @property changedSources changed implementation paths that activated it.
 * @property requiredDocumentation accepted documentation path patterns.
 */
data class DocumentationCoverageViolation(
    val rule: String,
    val changedSources: List<String>,
    val requiredDocumentation: List<String>
)

/**
 * Complete result of validating repository documentation.
 *
 * @property validatedDocuments typed documents whose metadata contract was
 * evaluated.
 * @property errors structural or link failures that always block verification.
 * @property warnings non-blocking review-expiry findings.
 * @property coverageViolations implementation changes without a corresponding
 * canonical documentation update.
 */
data class DocumentationValidationResult(
    val validatedDocuments: List<String>,
    val errors: List<String>,
    val warnings: List<String>,
    val coverageViolations: List<DocumentationCoverageViolation>
) {
    /** Whether no structural or coverage failure was found. */
    val isSuccessful: Boolean
        get() = errors.isEmpty() && coverageViolations.isEmpty()
}
