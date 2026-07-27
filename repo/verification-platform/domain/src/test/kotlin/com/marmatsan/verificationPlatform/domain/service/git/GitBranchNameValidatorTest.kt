package com.marmatsan.verificationPlatform.domain.service.git

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class GitBranchNameValidatorTest :
    FunSpec(
        {
            val validator = GitBranchNameValidator()

            test("normalizes supported local and remote Git prefixes") {
                validator.validate("refs/heads/feature/plant-reminders").branch shouldBe
                    "feature/plant-reminders"
                validator.validate("refs/remotes/origin/fix/watering-date").branch shouldBe
                    "fix/watering-date"
                validator.validate("origin/chore/update-gradle").branch shouldBe
                    "chore/update-gradle"
            }

            test("rejects release versions with leading zeroes") {
                validator.validate("release/01.4.0").valid shouldBe false
                validator.validate("release/1.04.0").valid shouldBe false
                validator.validate("release/1.4.00").valid shouldBe false
            }

            test("reports the allowed branch contract for invalid input") {
                val result = validator.validate("feature/")

                result.valid shouldBe false
                result.message shouldBe
                    "Branch 'feature/' violates the Git workflow. Expected main, " +
                    "feature/<kebab-case>, fix/<kebab-case>, chore/<kebab-case>, " +
                    "release/<x.y.z>, or hotfix/<kebab-case>."
            }
        },
    )
