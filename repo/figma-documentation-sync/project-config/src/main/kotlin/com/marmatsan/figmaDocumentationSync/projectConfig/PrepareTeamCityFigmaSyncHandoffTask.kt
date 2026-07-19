package com.marmatsan.figmaDocumentationSync.projectConfig

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/** Gradle entry point for preparing an official TeamCity Figma Sync handoff. */
@DisableCachingByDefault(because = "Downloads and inspects official TeamCity artifacts")
abstract class PrepareTeamCityFigmaSyncHandoffTask : DefaultTask() {
    @get:Input
    @get:Optional
    abstract val buildId: Property<Long>

    @get:InputDirectory
    @get:Optional
    abstract val artifactDirectory: DirectoryProperty

    @get:Internal
    abstract val destinationRoot: DirectoryProperty

    @get:Input
    @get:Optional
    abstract val expectedGitSha: Property<String>

    @get:Input
    abstract val mainBranchAliases: ListProperty<String>

    @get:Input
    abstract val requiredBuildTypeName: Property<String>

    @TaskAction
    fun prepare() {
        val result = TeamCityFigmaSyncHandoffPreparer().prepare(
            TeamCityFigmaSyncHandoffPreparer.Request(
                buildId = buildId.orNull,
                artifactDirectory = artifactDirectory.orNull?.asFile,
                destinationRoot = destinationRoot.get().asFile,
                expectedGitSha = expectedGitSha.orNull,
                mainBranchAliases = mainBranchAliases.get().toSet(),
                requiredBuildTypeName = requiredBuildTypeName.get()
            )
        )
        logger.lifecycle("Figma Sync handoff prepared: ${result.summaryFile.path}")
    }
}
