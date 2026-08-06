package com.marmatsan.verificationPlatform.domain.service.documentation

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class DocumentationPathResolverTest :
    FunSpec(
        {
            test("preserves hidden repository directory names") {
                DocumentationPathResolver().normalize(
                    path = ".agents/skills/add-feature/SKILL.md"
                ) shouldBe ".agents/skills/add-feature/SKILL.md"
            }

            test("removes only explicit relative and root path prefixes") {
                val resolver = DocumentationPathResolver()

                resolver.normalize(
                    path = "./docs/standards/example.md"
                ) shouldBe "docs/standards/example.md"
                resolver.normalize(
                    path = "/docs/standards/example.md"
                ) shouldBe "docs/standards/example.md"
            }
        }
    )
