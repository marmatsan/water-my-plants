package com.marmatsan.verificationPlatform.domain.service.documentation

import com.marmatsan.verificationPlatform.domain.model.documentation.DocumentationCoverageRule
import com.marmatsan.verificationPlatform.domain.model.documentation.DocumentationFile
import com.marmatsan.verificationPlatform.domain.model.documentation.DocumentationRepositorySnapshot
import com.marmatsan.verificationPlatform.domain.service.documentation.DocumentationValidator
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class DocumentationValidatorTest :
    FunSpec(
        {
            test("valid typed documents and superseded ADRs pass") {
                val result =
                    validate(
                        documents =
                            listOf(
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
                                ),
                                DocumentationFile(
                                    path = "specs/001-example-change/spec.md",
                                    content = validSpecification
                                ),
                                DocumentationFile(
                                    path = "specs/001-example-change/plan.md",
                                    content = validImplementationPlan
                                ),
                                DocumentationFile(
                                    path = "specs/001-example-change/checklist.md",
                                    content = validChecklist
                                )
                            ),
                        entries = setOf("source.txt")
                    )

                result.errors shouldBe emptyList()
                result.coverageViolations shouldBe emptyList()
                result.validatedDocuments shouldContainExactly
                    listOf(
                        "docs/decisions/adr-0001-historical-decision.md",
                        "docs/runbooks/example.md",
                        "docs/standards/example.md",
                        "specs/001-example-change/checklist.md",
                        "specs/001-example-change/plan.md",
                        "specs/001-example-change/spec.md"
                    )
            }

            test("typed documents outside canonical directories fail") {
                val misplacedGuide =
                    validStandard.replace(
                        "type: standard",
                        "type: guide"
                    )

                val result =
                    validate(
                        documents =
                            listOf(
                                DocumentationFile(
                                    path = "docs/misplaced.md",
                                    content = misplacedGuide
                                )
                            ),
                        entries = setOf("source.txt")
                    )

                result.errors shouldContain "[docs/misplaced.md] Typed document is outside its canonical directory."
            }

            test("composes an additional typed-document rule without changing the coordinator") {
                val customWarning = "[docs/standards/example.md] Custom rule executed."
                val validator =
                    DocumentationValidator(
                        classifier = DocumentationTypeClassifier(),
                        frontmatterParser = DocumentationFrontmatterParser(),
                        agentSkillValidator = AgentSkillValidator(),
                        typedRules =
                            listOf(
                                TypedDocumentationRule { _, findings ->
                                    findings.warnings += customWarning
                                }
                            ),
                        linkValidator = DocumentationLinkValidator(),
                        coverageValidator = DocumentationCoverageValidator(DocumentationPathResolver()),
                        paths = DocumentationPathResolver()
                    )

                val result =
                    validator.validate(
                        snapshot =
                            DocumentationRepositorySnapshot(
                                documents =
                                    listOf(
                                        DocumentationFile(
                                            path = "docs/standards/example.md",
                                            content = validStandard
                                        )
                                    ),
                                repositoryEntries =
                                    setOf(
                                        "source.txt",
                                        "docs/standards/example.md"
                                    )
                            ),
                        currentDate = today
                    )

                result.warnings shouldContainExactly listOf(customWarning)
            }

            test("runbooks require recovery guidance") {
                val incomplete =
                    validRunbook.replace(
                        "## Recovery\nExample.\n",
                        ""
                    )

                val result =
                    validate(
                        documents =
                            listOf(
                                DocumentationFile(
                                    path = "docs/runbooks/incomplete.md",
                                    content = incomplete
                                )
                            ),
                        entries = setOf("source.txt")
                    )

                result.errors shouldContain "[docs/runbooks/incomplete.md] Runbook section 'Recovery' is required."
            }

            test("active specifications require their file-specific sections") {
                val incomplete =
                    validSpecification.replace(
                        "## Acceptance Criteria\nExample.\n",
                        ""
                    )

                val result =
                    validate(
                        documents =
                            listOf(
                                DocumentationFile(
                                    path = "specs/001-example-change/spec.md",
                                    content = incomplete
                                )
                            ),
                        entries = setOf("source.txt")
                    )

                result.errors shouldContain
                    "[specs/001-example-change/spec.md] Specification section 'acceptance criteria' is required."
            }

            test("repository skills require a matching name and supported trigger metadata") {
                val result =
                    validate(
                        documents =
                            listOf(
                                DocumentationFile(
                                    path = ".agents/skills/add-feature/SKILL.md",
                                    content =
                                        validSkill.replace(
                                            "name: add-feature",
                                            "name: wrong-name\nowner: engineering"
                                        )
                                )
                            ),
                        entries = setOf("source.txt")
                    )

                result.errors shouldContain
                    "[.agents/skills/add-feature/SKILL.md] Skill name 'wrong-name' does not match its directory 'add-feature'."
                result.errors shouldContain
                    "[.agents/skills/add-feature/SKILL.md] Unsupported skill frontmatter field 'owner'."
            }

            test("repository skills require the canonical one-level kebab-case path") {
                val result =
                    validate(
                        documents =
                            listOf(
                                DocumentationFile(
                                    path = ".agents/skills/add_feature/SKILL.md",
                                    content = validSkill
                                )
                            ),
                        entries = setOf("source.txt")
                    )

                result.errors shouldContain
                    "[.agents/skills/add_feature/SKILL.md] " +
                    "Skill must use .agents/skills/<kebab-case-name>/SKILL.md."
            }

            test("broken local Markdown links fail") {
                val result =
                    validate(
                        documents =
                            listOf(
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
                val rules =
                    listOf(
                        DocumentationCoverageRule(
                            id = "example-rule",
                            sourcePaths = listOf("src/*"),
                            documentationPaths = listOf("docs/standards/example.md")
                        )
                    )
                val snapshot =
                    DocumentationRepositorySnapshot(
                        documents =
                            listOf(
                                DocumentationFile(
                                    path = "docs/standards/example.md",
                                    content = validStandard
                                )
                            ),
                        repositoryEntries =
                            setOf(
                                "source.txt",
                                "docs",
                                "docs/standards",
                                "docs/standards/example.md"
                            )
                    )

                val missing =
                    DocumentationValidator().validate(
                        snapshot = snapshot,
                        coverageRules = rules,
                        changedPaths = listOf("src/feature.kt"),
                        currentDate = today
                    )
                val satisfied =
                    DocumentationValidator().validate(
                        snapshot = snapshot,
                        coverageRules = rules,
                        changedPaths =
                            listOf(
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
        private val today =
            LocalDate.of(
                2026,
                7,
                20
            )

        private fun validate(
            documents: List<DocumentationFile>,
            entries: Set<String>
        ) = DocumentationValidator().validate(
            snapshot =
                DocumentationRepositorySnapshot(
                    documents = documents,
                    repositoryEntries = entries + documents.map { document -> document.path }
                ),
            currentDate = today
        )

        private val validStandard =
            """
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

        private val validRunbook =
            """
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

        private val supersededAdr =
            """
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

        private val validSpecification =
            """
            ---
            title: Example specification
            type: specification
            scope: repository
            owner: engineering
            status: active
            last-reviewed: 2026-07-18
            review-cycle-days: 30
            sources:
              - source.txt
            ---

            # Example Specification

            ## Outcome
            Example.
            ## Context
            Example.
            ## Required Behavior
            Example.
            ## Acceptance Criteria
            Example.
            ## Non-Goals
            Example.
            ## Decision Log
            Example.
            ## Sources
            Example.
            """.trimIndent()

        private val validImplementationPlan =
            """
            ---
            title: Example implementation plan
            type: specification
            scope: repository
            owner: engineering
            status: active
            last-reviewed: 2026-07-18
            review-cycle-days: 30
            sources:
              - source.txt
            ---

            # Example Implementation Plan

            ## Outcome
            Example.
            ## Steps
            Example.
            ## Verification
            Example.
            ## Decision Documentation
            Example.
            """.trimIndent()

        private val validChecklist =
            """
            ---
            title: Example checklist
            type: specification
            scope: repository
            owner: engineering
            status: active
            last-reviewed: 2026-07-18
            review-cycle-days: 30
            sources:
              - source.txt
            ---

            # Example Checklist

            ## Scope
            Example.
            ## Implementation
            Example.
            ## Architecture And SOLID
            Example.
            ## Testing And Verification
            Example.
            ## Documentation
            Example.
            ## Completion
            Example.
            """.trimIndent()

        private val validSkill =
            """
            ---
            name: add-feature
            description: Add one product feature. Use for product capability implementation.
            ---

            # Add Feature

            Follow the canonical feature guide.
            """.trimIndent()
    }
}
