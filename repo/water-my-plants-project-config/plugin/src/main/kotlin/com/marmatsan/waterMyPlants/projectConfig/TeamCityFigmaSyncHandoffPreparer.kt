package com.marmatsan.waterMyPlants.projectConfig

import com.marmatsan.figmaDocumentationSync.teamcityAdapter.TeamCityBuildArtifactClient
import com.marmatsan.figmaDocumentationSync.teamcityAdapter.TeamCityCliClient
import kotlinx.serialization.json.JsonObject
import java.io.File
import java.time.Clock

/** Orchestrates the focused ports that prepare one validated TeamCity/Figma handoff. */
class TeamCityFigmaSyncHandoffPreparer internal constructor(
    private val artifactDirectoryResolver: TeamCityFigmaArtifactDirectoryResolver,
    private val artifactSetSource: CanonicalFigmaArtifactSetSource,
    private val artifactVerifier: CanonicalFigmaArtifactVerifier,
    private val runnerInspector: CanonicalFigmaRunnerInspector,
    private val summaryFactory: TeamCityFigmaHandoffSummaryFactory,
    private val summaryWriter: TeamCityFigmaHandoffSummaryWriter,
) {
    constructor(
        teamCityClient: TeamCityBuildArtifactClient = TeamCityCliClient(),
        clock: Clock = Clock.systemUTC(),
    ) : this(
        artifactDirectoryResolver =
            TeamCityFigmaArtifactDirectoryResolver(
                teamCityClient = teamCityClient,
                archiveExtractor = SafeZipArchiveExtractor(),
                clock = clock,
            ),
        artifactSetSource = DefaultCanonicalFigmaArtifactSetSource(),
        artifactVerifier = DefaultCanonicalFigmaArtifactVerifier(),
        runnerInspector = DefaultCanonicalFigmaRunnerInspector(),
        summaryFactory = TeamCityFigmaHandoffSummaryFactory(clock),
        summaryWriter = JsonTeamCityFigmaHandoffSummaryWriter(),
    )

    fun prepare(
        request: Request,
    ): Result {
        val artifactDirectory = artifactDirectoryResolver.resolve(request)
        val artifacts = artifactSetSource.read(artifactDirectory)
        val validated =
            artifactVerifier.verify(
                artifacts.contract,
                request.expectedGitSha,
            )
        val visualManifest =
            requireNotNull(artifacts.visualManifestPath) {
                "Canonical artifact set does not contain one visual manifest."
            }
        requireNotNull(artifacts.metadataManifestPath) {
            "Canonical artifact set does not contain one metadata manifest."
        }
        val inspection =
            runnerInspector.inspect(
                visualManifest,
                artifacts.planPath,
            )
        val summary =
            summaryFactory.create(
                request = request,
                artifacts = artifacts,
                validated = validated,
                inspection = inspection,
            )
        val summaryFile =
            summaryWriter.write(
                artifacts.artifactDirectory.toFile(),
                summary,
            )
        return Result(
            artifactDirectory = artifactDirectory,
            summaryFile = summaryFile,
            summary = summary,
        )
    }

    data class Request(
        val buildId: Long?,
        val artifactDirectory: File?,
        val destinationRoot: File,
        val expectedGitSha: String? = null,
        val mainBranchAliases: Set<String> =
            setOf(
                "main",
                "<default>",
                "refs/heads/main",
            ),
        val requiredBuildTypeName: String = "Generate main design model",
    )

    data class Result(
        val artifactDirectory: File,
        val summaryFile: File,
        val summary: JsonObject,
    )
}
