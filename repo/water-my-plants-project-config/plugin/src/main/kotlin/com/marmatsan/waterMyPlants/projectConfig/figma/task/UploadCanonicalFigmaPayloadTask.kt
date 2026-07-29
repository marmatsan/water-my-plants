package com.marmatsan.waterMyPlants.projectConfig.figma.task

import com.marmatsan.figmaDocumentationSync.teamcityAdapter.TeamCityCliClient
import com.marmatsan.waterMyPlants.projectConfig.figma.handoff.TeamCityFigmaSyncHandoffPreparer
import com.marmatsan.waterMyPlants.projectConfig.figma.sync.TeamCityCanonicalFigmaPayloadUploader
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

/** Gradle entry point for uploading one verified canonical TeamCity PNG payload. */
@DisableCachingByDefault(
    because = "Downloads a canonical artifact and uploads its PNG to Figma"
)
abstract class UploadCanonicalFigmaPayloadTask : DefaultTask() {
    /** Optional TeamCity build to download, mutually exclusive with [artifactDirectory]. */
    @get:Input
    @get:Optional
    abstract val buildId: Property<Long>

    /** Optional existing artifact set, mutually exclusive with [buildId]. */
    @get:InputDirectory
    @get:Optional
    abstract val artifactDirectory: DirectoryProperty

    /** Secret single-use Figma MCP upload URL excluded from cache inputs. */
    @get:Internal
    abstract val uploadUrl: Property<String>

    /** Safe root beneath which downloaded artifacts are extracted. */
    @get:Internal
    abstract val destinationRoot: DirectoryProperty

    /** Project directory used as the TeamCity CLI working directory. */
    @get:Internal
    abstract val projectDirectory: DirectoryProperty

    /** Required repository revision when existing local artifacts are supplied. */
    @get:Input
    @get:Optional
    abstract val expectedGitSha: Property<String>

    /** Accepted TeamCity spellings of the canonical `main` branch. */
    @get:Input
    abstract val mainBranchAliases: ListProperty<String>

    /** Build configuration name allowed to publish production artifacts. */
    @get:Input
    abstract val requiredBuildTypeName: Property<String>

    /** Validates all artifact and payload identities before the single upload side effect. */
    @TaskAction
    fun upload() {
        val artifacts = artifactDirectory.orNull?.asFile
        require((buildId.orNull == null) xor (artifacts == null)) {
            "Configure exactly one of figmaTeamCityBuildId or figmaArtifactDirectory."
        }
        if (artifacts != null) {
            require(!expectedGitSha.orNull.isNullOrBlank()) {
                "figmaExpectedGitSha is required with figmaArtifactDirectory."
            }
        }
        val teamCityClient =
            TeamCityCliClient(
                workingDirectory = projectDirectory.get().asFile
            )
        val handoffPreparer =
            TeamCityFigmaSyncHandoffPreparer(
                teamCityClient = teamCityClient
            )
        val result =
            TeamCityCanonicalFigmaPayloadUploader(
                handoffPreparer = handoffPreparer
            ).upload(
                TeamCityCanonicalFigmaPayloadUploader.Request(
                    buildId = buildId.orNull,
                    artifactDirectory = artifacts,
                    uploadUrl = uploadUrl.get(),
                    destinationRoot = destinationRoot.get().asFile,
                    expectedGitSha = expectedGitSha.orNull,
                    mainBranchAliases = mainBranchAliases.get().toSet(),
                    requiredBuildTypeName = requiredBuildTypeName.get()
                )
            )
        logger.lifecycle(
            "Uploaded canonical Figma payload from " +
                (
                    result.buildId?.let { build -> "TeamCity build $build" }
                        ?: "the validated artifact directory"
                ) + ": " +
                "${result.payloadFileName} (${result.payloadByteLength} bytes, " +
                "${result.payloadSha256}); gitSha=${result.gitSha}, modelHash=${result.modelHash}."
        )
    }
}
