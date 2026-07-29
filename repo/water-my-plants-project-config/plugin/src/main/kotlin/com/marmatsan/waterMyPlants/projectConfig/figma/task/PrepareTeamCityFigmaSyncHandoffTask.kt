package com.marmatsan.waterMyPlants.projectConfig.figma.task

import com.marmatsan.waterMyPlants.projectConfig.figma.handoff.TeamCityFigmaSyncHandoffPreparer
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

/** Gradle entry point for preparing a canonical TeamCity Figma Sync handoff. */
@DisableCachingByDefault(
    because = "Downloads and inspects canonical TeamCity artifacts"
)
abstract class PrepareTeamCityFigmaSyncHandoffTask : DefaultTask() {
    /** Optional TeamCity build to download, mutually exclusive with [artifactDirectory]. */
    @get:Input
    @get:Optional
    abstract val buildId: Property<Long>

    /** Optional existing artifacts, mutually exclusive with [buildId]. */
    @get:InputDirectory
    @get:Optional
    abstract val artifactDirectory: DirectoryProperty

    /** Safe root beneath which downloaded archives are extracted. */
    @get:Internal
    abstract val destinationRoot: DirectoryProperty

    /** Optional repository revision that the canonical artifact contract must match. */
    @get:Input
    @get:Optional
    abstract val expectedGitSha: Property<String>

    /** Accepted TeamCity spellings of the canonical `main` branch. */
    @get:Input
    abstract val mainBranchAliases: ListProperty<String>

    /** Build configuration name allowed to publish the production artifact set. */
    @get:Input
    abstract val requiredBuildTypeName: Property<String>

    /** Downloads when necessary, validates identity, and writes an operator handoff summary. */
    @TaskAction
    fun prepare() {
        val result =
            TeamCityFigmaSyncHandoffPreparer().prepare(
                request =
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
