package com.marmatsan.figmaDocumentationSync.teamcity.operations.handoff

import com.marmatsan.figmaDocumentationSync.teamcity.operations.handoff.adapter.DefaultCanonicalFigmaArtifactSetSource
import com.marmatsan.figmaDocumentationSync.teamcity.operations.handoff.adapter.DefaultCanonicalFigmaArtifactVerifier
import com.marmatsan.figmaDocumentationSync.teamcity.operations.handoff.adapter.DefaultCanonicalFigmaRunnerInspector
import com.marmatsan.figmaDocumentationSync.teamcity.operations.handoff.adapter.JsonTeamCityFigmaHandoffSummaryWriter
import com.marmatsan.figmaDocumentationSync.teamcity.operations.handoff.adapter.SafeZipArchiveExtractor
import com.marmatsan.figmaDocumentationSync.teamcity.operations.handoff.port.CanonicalFigmaArtifactSetSource
import com.marmatsan.figmaDocumentationSync.teamcity.operations.handoff.port.CanonicalFigmaArtifactVerifier
import com.marmatsan.figmaDocumentationSync.teamcity.operations.handoff.port.CanonicalFigmaRunnerInspector
import com.marmatsan.figmaDocumentationSync.teamcity.operations.handoff.port.TeamCityFigmaHandoffSummaryWriter
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
    private val summaryWriter: TeamCityFigmaHandoffSummaryWriter
) {
    constructor(
        teamCityClient: TeamCityBuildArtifactClient = TeamCityCliClient(),
        clock: Clock = Clock.systemUTC()
    ) : this(
        artifactDirectoryResolver =
            TeamCityFigmaArtifactDirectoryResolver(
                teamCityClient = teamCityClient,
                archiveExtractor = SafeZipArchiveExtractor(),
                clock = clock
            ),
        artifactSetSource = DefaultCanonicalFigmaArtifactSetSource(),
        artifactVerifier = DefaultCanonicalFigmaArtifactVerifier(),
        runnerInspector = DefaultCanonicalFigmaRunnerInspector(),
        summaryFactory = TeamCityFigmaHandoffSummaryFactory(clock),
        summaryWriter = JsonTeamCityFigmaHandoffSummaryWriter()
    )

    /** Resolves, validates, inspects, and summarizes one canonical handoff [request]. */
    fun prepare(
        request: Request
    ): PreparedHandoff {
        val artifactDirectory = artifactDirectoryResolver.resolve(request)
        val artifacts = artifactSetSource.read(artifactDirectory)
        val validated =
            artifactVerifier.verify(
                artifacts.contract,
                request.expectedGitSha
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
                artifacts.planPath
            )
        val summary =
            summaryFactory.create(
                request = request,
                artifacts = artifacts,
                validated = validated,
                inspection = inspection
            )
        val summaryFile =
            summaryWriter.write(
                artifacts.artifactDirectory.toFile(),
                summary
            )
        return PreparedHandoff(
            artifactDirectory = artifactDirectory,
            summaryFile = summaryFile,
            summary = summary
        )
    }

    /**
     * Inputs that select and constrain a canonical Figma artifact handoff.
     *
     * @property buildId TeamCity build to download, mutually exclusive with [artifactDirectory].
     * @property artifactDirectory existing local artifacts, mutually exclusive with [buildId].
     * @property destinationRoot safe root for downloaded and extracted artifacts.
     * @property expectedGitSha optional repository revision required by the artifact contract.
     * @property mainBranchAliases accepted TeamCity spellings of the canonical branch.
     * @property requiredBuildTypeName build configuration allowed to publish production artifacts.
     */
    data class Request(
        val buildId: Long?,
        val artifactDirectory: File?,
        val destinationRoot: File,
        val expectedGitSha: String? = null,
        val mainBranchAliases: Set<String> =
            setOf(
                "main",
                "<default>",
                "refs/heads/main"
            ),
        val requiredBuildTypeName: String = "Generate main design model"
    )

    /**
     * Prepared artifact set and its materialized operator summary.
     *
     * @property artifactDirectory normalized directory containing the validated artifact set.
     * @property summaryFile JSON handoff written beside the artifacts.
     * @property summary in-memory handoff contract returned to programmatic consumers.
     */
    data class PreparedHandoff(
        val artifactDirectory: File,
        val summaryFile: File,
        val summary: JsonObject
    )
}
