package com.marmatsan.waterMyPlants.projectConfig.figma.handoff

import com.marmatsan.figmaDocumentationSync.teamcityAdapter.TeamCityBuildArtifactClient
import com.marmatsan.waterMyPlants.projectConfig.figma.handoff.port.ArtifactArchiveExtractor
import java.io.File
import java.nio.file.Files
import java.time.Clock
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/** Resolves a validated local artifact directory from either TeamCity or an explicit path. */
internal class TeamCityFigmaArtifactDirectoryResolver(
    private val teamCityClient: TeamCityBuildArtifactClient,
    private val archiveExtractor: ArtifactArchiveExtractor,
    private val clock: Clock,
) {
    /** Selects existing artifacts or downloads and safely expands the requested TeamCity build. */
    fun resolve(
        request: TeamCityFigmaSyncHandoffPreparer.Request,
    ): File {
        require((request.buildId == null) xor (request.artifactDirectory == null)) {
            "Configure exactly one of figmaTeamCityBuildId or figmaArtifactDirectory."
        }
        request.artifactDirectory?.let { directory ->
            require(directory.isDirectory) { "Artifact directory does not exist: ${directory.path}" }
            return directory
                .toPath()
                .toAbsolutePath()
                .normalize()
                .toFile()
        }
        return download(
            request = request,
        )
    }

    private fun download(
        request: TeamCityFigmaSyncHandoffPreparer.Request,
    ): File {
        val buildId = requireNotNull(request.buildId)
        val build = teamCityClient.readBuild(buildId)
        require(build.id == buildId) {
            "TeamCity returned build ${build.id} while build $buildId was requested."
        }
        require(build.state == "finished" && build.status == "SUCCESS") {
            "Build $buildId must be finished and successful; found state '${build.state}' " +
                "and status '${build.status}'."
        }
        require(build.branchName in request.mainBranchAliases) {
            "Build $buildId is not from main; found branch '${build.branchName}'."
        }
        require(build.buildTypeName.contains(request.requiredBuildTypeName)) {
            "Build $buildId is '${build.buildTypeName}', not the ${request.requiredBuildTypeName} job."
        }
        val timestamp = downloadTimestamp.format(clock.instant())
        val output = request.destinationRoot.resolve("figma-sync-$buildId-$timestamp")
        require(output.mkdirs()) { "Could not create TeamCity artifact directory: ${output.path}" }
        teamCityClient.downloadArtifacts(
            buildId,
            output,
        )
        expandSharedArchiveWhenNeeded(
            directory = output,
        )
        return output
            .toPath()
            .toAbsolutePath()
            .normalize()
            .toFile()
    }

    private fun expandSharedArchiveWhenNeeded(
        directory: File,
    ) {
        val models = directory.findFiles("design-model.json")
        val archives = directory.findFiles(".shared_files.zip")
        if (models.isEmpty() && archives.size == 1) {
            archiveExtractor.extract(
                archive = archives.single(),
                destination = directory.resolve("shared-files"),
            )
        } else {
            require(archives.size <= 1) {
                "TeamCity returned more than one .shared_files.zip artifact."
            }
        }
    }

    private companion object {
        val downloadTimestamp: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneOffset.UTC)
    }
}

private fun File.findFiles(
    fileName: String,
): List<File> =
    Files.walk(toPath()).use { paths ->
        paths
            .filter { path -> Files.isRegularFile(path) && path.fileName.toString() == fileName }
            .map(java.nio.file.Path::toFile)
            .toList()
    }
