package com.marmatsan.verificationPlatform.plugin

import com.marmatsan.verificationPlatform.data.git.GitCurrentBranchSource
import com.marmatsan.verificationPlatform.domain.service.GitBranchNameValidator
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/** Validates the current branch against the repository Git workflow. */
@DisableCachingByDefault(
    because = "Git branch validation depends on checkout state"
)
abstract class CheckGitWorkflowTask : DefaultTask() {
    /** Repository checkout whose branch is validated. */
    @get:Internal
    abstract val repositoryRoot: DirectoryProperty

    /** Optional full branch ref supplied by a CI provider for detached HEAD. */
    @get:Input
    @get:Optional
    abstract val branchOverride: Property<String>

    /** Resolves and validates the current branch name. */
    @TaskAction
    fun checkGitWorkflow() {
        val branch = GitCurrentBranchSource().read(
            repositoryRoot = repositoryRoot.get().asFile,
            branchOverride = branchOverride.orNull
        )
        val validation = GitBranchNameValidator().validate(branch)
        check(validation.valid) { validation.message ?: "Git workflow validation failed." }

        val source = if (validation.providerManaged) "provider-managed ref" else "repository branch"
        logger.lifecycle(
            "Git workflow validation passed for {} '{}'.",
            source,
            validation.branch
        )
    }
}
