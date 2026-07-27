package com.marmatsan.verificationPlatform.domain.bdd

import com.marmatsan.verificationPlatform.domain.model.git.GitBranchValidation
import com.marmatsan.verificationPlatform.domain.service.git.GitBranchNameValidator
import io.cucumber.java8.En
import io.kotest.matchers.shouldBe

class GitWorkflowSteps : En {
    private lateinit var branch: String
    private lateinit var validation: GitBranchValidation

    init {
        Given("the Git branch is {string}") { value: String ->
            branch = value
        }
        When("the Git branch name is validated") {
            validation = GitBranchNameValidator().validate(branch)
        }
        Then("the Git branch is accepted") {
            validation.valid shouldBe true
        }
        Then("the Git branch is rejected") {
            validation.valid shouldBe false
        }
        Then("the Git branch is provider-managed") {
            validation.providerManaged shouldBe true
        }
    }
}
