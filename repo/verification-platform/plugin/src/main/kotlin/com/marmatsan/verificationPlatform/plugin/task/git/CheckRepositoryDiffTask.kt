package com.marmatsan.verificationPlatform.plugin.task.git

import com.marmatsan.verificationPlatform.data.git.GitRepositoryDiffChecker
import com.marmatsan.verificationPlatform.data.json.CiPlanJson
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/** Runs the committed whitespace diff check selected by the CI plan. */
@DisableCachingByDefault(
    because = "Git diff verification depends on committed repository state"
)
abstract class CheckRepositoryDiffTask : DefaultTask() {
    /** Repository checkout whose committed diff is verified. */
    @get:Internal
    abstract val repositoryRoot: DirectoryProperty

    /** CI plan containing the exact comparison and head revisions. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val planFile: RegularFileProperty

    /** Verifies the plan revision range using `git diff --check`. */
    @TaskAction
    fun checkRepositoryDiff() {
        val plan = CiPlanJson().read(planFile.get().asFile.readText())
        val comparisonBase =
            checkNotNull(plan.comparisonBase) {
                "Repository diff verification requires a comparison base."
            }
        GitRepositoryDiffChecker().check(
            repositoryRoot = repositoryRoot.get().asFile,
            comparisonBase = comparisonBase,
            head = plan.head
        )
        logger.lifecycle(
            "Repository diff verification passed for {}..{}.",
            comparisonBase,
            plan.head
        )
    }
}
