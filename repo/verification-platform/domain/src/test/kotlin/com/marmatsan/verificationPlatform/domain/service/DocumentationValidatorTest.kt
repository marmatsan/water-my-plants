package com.marmatsan.verificationPlatform.domain.service

import com.marmatsan.verificationPlatform.domain.model.DocumentationCoverageRule
import com.marmatsan.verificationPlatform.domain.model.DocumentationFile
import com.marmatsan.verificationPlatform.domain.model.DocumentationRepositorySnapshot
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class DocumentationValidatorTest : FunSpec(
    {
    test("valid typed documents and superseded ADRs pass") {
        val result = validate(
            documents = listOf(
                DocumentationFile(
                    path = "docs/standards/example.md",
                    content = validStandard
                ),
                DocumentationFile(
                    path = "docs/runbooks/example.md",
                    content = validRunbook
                ),
                DocumentationFile(
                    path = "docs/decisions/adr-0001-historical-decision.md",
                    content = supersededAdr
                )
            ),
            entries = setOf("source.txt")
        )

        result.errors shouldBe emptyList()
        result.coverageViolations shouldBe emptyList()
        result.validatedDocuments shouldContainExactly listOf(
            "docs/decisions/adr-0001-historical-decision.md",
            "docs/runbooks/example.md",
            "docs/standards/example.md"
        )
    }

    test("typed documents outside canonical directories fail") {
        val misplacedGuide = validStandard.replace(
            "type: standard",
            "type: guide"
        )

        val result = validate(
            documents = listOf(
                DocumentationFile(
                    path = "docs/misplaced.md",
                    content = misplacedGuide
                )
            ),
            entries = setOf("source.txt")
        )

        result.errors shouldContain "[docs/misplaced.md] Typed document is outside its canonical directory."
    }

    test("runbooks require recovery guidance") {
        val incomplete = validRunbook.replace(
            "## Recovery\nExample.\n",
            ""
        )

        val result = validate(
            documents = listOf(
                DocumentationFile(
                    path = "docs/runbooks/incomplete.md",
                    content = incomplete
                )
            ),
            entries = setOf("source.txt")
        )

        result.errors shouldContain "[docs/runbooks/incomplete.md] Runbook section 'Recovery' is required."
    }

    test("broken local Markdown links fail") {
        val result = validate(
            documents = listOf(
                DocumentationFile(
                    path = "docs/standards/broken-link.md",
                    content = "$validStandard\n[Missing](missing.md)\n"
                )
            ),
            entries = setOf("source.txt")
        )

        result.errors shouldContain
            "[docs/standards/broken-link.md] Broken local Markdown link: missing.md"
    }

    test("coverage rules require a mapped documentation change") {
        val rules = listOf(
            DocumentationCoverageRule(
                id = "example-rule",
                sourcePaths = listOf("src/*"),
                documentationPaths = listOf("docs/standards/example.md")
            )
        )
        val snapshot = DocumentationRepositorySnapshot(
            documents = listOf(
                DocumentationFile(
                    path = "docs/standards/example.md",
                    content = validStandard
                )
            ),
            repositoryEntries = setOf(
                "source.txt",
                "docs",
                "docs/standards",
                "docs/standards/example.md"
            )
        )

        val missing = DocumentationValidator().validate(
            snapshot = snapshot,
            coverageRules = rules,
            changedPaths = listOf("src/feature.kt"),
            currentDate = today
        )
        val satisfied = DocumentationValidator().validate(
            snapshot = snapshot,
            coverageRules = rules,
            changedPaths = listOf(
                "src/feature.kt",
                "docs/standards/example.md"
            ),
            currentDate = today
        )

        missing.coverageViolations.map { violation -> violation.rule } shouldBe listOf("example-rule")
        satisfied.coverageViolations shouldBe emptyList()
    }
}
) {
    companion object {
        private val today = LocalDate.of(
            2026,
            7,
            20
        )

        private fun validate(
            documents: List<DocumentationFile>,
            entries: Set<String>
        ) = DocumentationValidator().validate(
            snapshot = DocumentationRepositorySnapshot(
                documents = documents,
                repositoryEntries = entries + documents.map { document -> document.path }
            ),
            currentDate = today
        )

        private val validStandard = """
            ---
            title: Example standard
            type: standard
            scope: repository
            owner: engineering
            status: active
            last-reviewed: 2026-07-18
            review-cycle-days: 180
            sources:
              - source.txt
            ---

            # Example Standard

            ## Rules

            Example.
        """.trimIndent()

        private val validRunbook = """
            ---
            title: Example operation
            type: runbook
            scope: repository
            owner: operations
            status: active
            last-reviewed: 2026-07-18
            review-cycle-days: 90
            sources:
              - source.txt
            ---

            # Example Operation

            ## Purpose
            Example.
            ## Prerequisites
            Example.
            ## Procedure
            Example.
            ## Verification
            Example.
            ## Recovery
            Example.
            ## Prohibited Actions
            Example.
            ## Sources
            Example.
        """.trimIndent()

        private val supersededAdr = """
            ---
            title: Historical decision
            type: adr
            scope: repository
            owner: architecture
            status: superseded
            last-reviewed: 2026-07-19
            review-cycle-days: 365
            sources:
              - retired/source.kt
            ---

            # ADR-0001: Historical Decision

            ## Context
            Historical context.
            ## Decision
            Historical decision.
            ## Consequences
            Historical consequences.
            ## Alternatives
            Historical alternatives.
            ## Supersession
            Superseded by a current decision.
        """.trimIndent()
    }
}
