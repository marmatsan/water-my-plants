package com.marmatsan.ci.plugin

import com.marmatsan.ci.data.git.GitRepositoryChangeSetSource
import com.marmatsan.ci.data.json.CiPlanJson
import com.marmatsan.ci.domain.service.CiPlanFactory
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "The plan depends on Git revision state outside Gradle inputs")
abstract class GenerateCiPlanTask : DefaultTask() {
    @get:Internal
    abstract val repositoryRoot: DirectoryProperty

    @get:Optional
    @get:Input
    abstract val comparisonBaseOverride: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val changeSet = GitRepositoryChangeSetSource().read(
            repositoryRoot = repositoryRoot.get().asFile,
            comparisonBaseOverride = comparisonBaseOverride.orNull
        )
        val plan = CiPlanFactory().create(changeSet)
        val output = outputFile.get().asFile
        CiPlanJson().write(plan, output)

        logger.lifecycle(
            "CI plan generated: scope={}, fullVerification={}, output={}",
            plan.scope,
            plan.fullVerification,
            output.absolutePath
        )
    }
}
