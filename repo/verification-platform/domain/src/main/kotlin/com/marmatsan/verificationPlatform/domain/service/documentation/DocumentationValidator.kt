package com.marmatsan.verificationPlatform.domain.service.documentation

import com.marmatsan.verificationPlatform.domain.model.documentation.DocumentationCoverageRule
import com.marmatsan.verificationPlatform.domain.model.documentation.DocumentationRepositorySnapshot
import com.marmatsan.verificationPlatform.domain.model.documentation.DocumentationValidationResult
import com.marmatsan.verificationPlatform.domain.service.documentation.DocumentationCoverageValidator
import com.marmatsan.verificationPlatform.domain.service.documentation.DocumentationFindings
import com.marmatsan.verificationPlatform.domain.service.documentation.DocumentationFrontmatterParser
import com.marmatsan.verificationPlatform.domain.service.documentation.DocumentationHeadingValidator
import com.marmatsan.verificationPlatform.domain.service.documentation.DocumentationLinkValidator
import com.marmatsan.verificationPlatform.domain.service.documentation.DocumentationMetadataValidator
import com.marmatsan.verificationPlatform.domain.service.documentation.DocumentationPathResolver
import com.marmatsan.verificationPlatform.domain.service.documentation.DocumentationTypeClassifier
import com.marmatsan.verificationPlatform.domain.service.documentation.TypedDocumentationContext
import com.marmatsan.verificationPlatform.domain.service.documentation.TypedDocumentationRule
import java.time.LocalDate

/** Coordinates focused typed-document, link, and change-coverage validators. */
class DocumentationValidator internal constructor(
    private val classifier: DocumentationTypeClassifier,
    private val frontmatterParser: DocumentationFrontmatterParser,
    private val typedRules: List<TypedDocumentationRule>,
    private val linkValidator: DocumentationLinkValidator,
    private val coverageValidator: DocumentationCoverageValidator,
    private val paths: DocumentationPathResolver,
) {
    constructor() : this(
        classifier = DocumentationTypeClassifier(),
        frontmatterParser = DocumentationFrontmatterParser(),
        typedRules =
            listOf(
                DocumentationMetadataValidator(),
                DocumentationHeadingValidator(),
            ),
        linkValidator = DocumentationLinkValidator(),
        coverageValidator = DocumentationCoverageValidator(),
        paths = DocumentationPathResolver(),
    )

    /**
     * Validates typed documentation, repository links, and change-coverage rules.
     *
     * @param snapshot repository documentation and entry snapshot to validate.
     * @param coverageRules rules that require documentation when matching paths change.
     * @param changedPaths changed repository paths, or `null` to skip coverage validation.
     * @param currentDate date used to evaluate review metadata deterministically.
     * @return all validated documents, validation findings, and coverage violations.
     */
    fun validate(
        snapshot: DocumentationRepositorySnapshot,
        coverageRules: List<DocumentationCoverageRule> = emptyList(),
        changedPaths: List<String>? = null,
        currentDate: LocalDate = LocalDate.now(),
    ): DocumentationValidationResult {
        val findings = DocumentationFindings()
        val validatedDocuments = mutableListOf<String>()
        val repositoryEntries = snapshot.repositoryEntries.map(paths::normalize).toSet()

        snapshot.documents.sortedBy { document -> document.path }.forEach { document ->
            val path = paths.normalize(document.path)
            val expectedType = classifier.expectedType(path)
            val frontmatter = frontmatterParser.parse(document.content)
            if (expectedType == null) {
                if (frontmatter?.metadata?.get("type") in classifier.typedDocumentTypes) {
                    findings.errors += "[$path] Typed document is outside its canonical directory."
                }
            } else {
                validatedDocuments += path
                if (frontmatter == null) {
                    findings.errors += "[$path] Typed document must start with YAML frontmatter."
                } else {
                    val context =
                        TypedDocumentationContext(
                            path = path,
                            expectedType = expectedType,
                            frontmatter = frontmatter,
                            repositoryEntries = repositoryEntries,
                            currentDate = currentDate,
                        )
                    typedRules.forEach { rule ->
                        rule.validate(
                            context,
                            findings,
                        )
                    }
                }
            }
            linkValidator.validate(
                path,
                document.content,
                repositoryEntries,
                findings,
            )
        }

        return DocumentationValidationResult(
            validatedDocuments = validatedDocuments,
            errors = findings.errors,
            warnings = findings.warnings,
            coverageViolations =
                changedPaths
                    ?.let { paths ->
                        coverageValidator.validate(
                            paths,
                            coverageRules,
                        )
                    }.orEmpty(),
        )
    }
}
