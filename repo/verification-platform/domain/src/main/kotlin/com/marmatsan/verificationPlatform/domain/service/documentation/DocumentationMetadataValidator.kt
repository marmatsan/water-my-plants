package com.marmatsan.verificationPlatform.domain.service.documentation

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/** Validates typed-document metadata, review windows, and canonical source references. */
internal class DocumentationMetadataValidator(
    private val paths: DocumentationPathResolver = DocumentationPathResolver(),
) : TypedDocumentationRule {
    override fun validate(
        context: TypedDocumentationContext,
        findings: DocumentationFindings,
    ) {
        val metadata = context.frontmatter.metadata
        REQUIRED_METADATA_FIELDS.forEach { field ->
            if (metadata[field].isNullOrBlank()) {
                findings.errors += "[${context.path}] Missing frontmatter field '$field'."
            }
        }
        val actualType = metadata["type"]
        if (actualType != context.expectedType) {
            findings.errors +=
                "[${context.path}] Frontmatter type '${actualType.orEmpty()}' " +
                "does not match path type '${context.expectedType}'."
        }
        val status = metadata["status"].orEmpty()
        if (status !in SUPPORTED_STATUSES) {
            findings.errors += "[${context.path}] Unsupported status '$status'."
        }
        PLACEHOLDER_FIELDS.forEach { field ->
            if (PLACEHOLDER_PATTERN.containsMatchIn(metadata[field].orEmpty())) {
                findings.errors += "[${context.path}] Frontmatter field '$field' still contains template text."
            }
        }
        val reviewDate = metadata["last-reviewed"].parseReviewDate()
        if (reviewDate == null) {
            findings.errors += "[${context.path}] last-reviewed must use YYYY-MM-DD."
        }
        validateReviewCycle(
            context = context,
            reviewDate = reviewDate,
            findings = findings,
        )
        validateSources(
            context = context,
            status = status,
            findings = findings,
        )
    }

    private fun validateReviewCycle(
        context: TypedDocumentationContext,
        reviewDate: LocalDate?,
        findings: DocumentationFindings,
    ) {
        val reviewCycleDays = context.frontmatter.metadata["review-cycle-days"]?.toIntOrNull()
        if (reviewCycleDays == null || reviewCycleDays <= 0) {
            findings.errors += "[${context.path}] review-cycle-days must be a positive integer."
        } else if (reviewDate != null && reviewDate.plusDays(reviewCycleDays.toLong()).isBefore(context.currentDate)) {
            findings.warnings +=
                "[${context.path}] Documentation review expired on " +
                "${reviewDate.plusDays(reviewCycleDays.toLong())}."
        }
    }

    private fun validateSources(
        context: TypedDocumentationContext,
        status: String,
        findings: DocumentationFindings,
    ) {
        if (context.frontmatter.sources.isEmpty()) {
            findings.errors += "[${context.path}] At least one canonical source is required."
        }
        if (status != "superseded") {
            context.frontmatter.sources.forEach { source ->
                if (!EXTERNAL_SOURCE_PATTERN.containsMatchIn(source) &&
                    !paths.sourceExists(
                        source,
                        context.repositoryEntries,
                    )
                ) {
                    findings.errors += "[${context.path}] Canonical source does not exist: $source"
                }
            }
        }
    }

    private fun String?.parseReviewDate(): LocalDate? =
        this?.let { value ->
            try {
                LocalDate.parse(
                    value,
                    DateTimeFormatter.ISO_LOCAL_DATE,
                )
            } catch (
                _: DateTimeParseException,
            ) {
                null
            }
        }

    private companion object {
        val SUPPORTED_STATUSES =
            setOf(
                "draft",
                "active",
                "accepted",
                "deprecated",
                "superseded",
            )
        val REQUIRED_METADATA_FIELDS =
            listOf(
                "title",
                "type",
                "scope",
                "owner",
                "status",
                "last-reviewed",
                "review-cycle-days",
            )
        val PLACEHOLDER_FIELDS =
            listOf(
                "title",
                "scope",
                "owner",
            )
        val PLACEHOLDER_PATTERN =
            Regex(
                "replace|repository-or|stable-area|placeholder",
                RegexOption.IGNORE_CASE,
            )
        val EXTERNAL_SOURCE_PATTERN = Regex("^(https?:|generated:)")
    }
}
