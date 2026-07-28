package com.marmatsan.figmaDocumentationSync.teamcityAdapter

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

/** TeamCity CLI adapter used by local Kotlin operational tasks. */
class TeamCityCliClient(
    private val environment: Map<String, String?> = emptyMap(),
    private val workingDirectory: File? = null,
    private val execute: (List<String>, Map<String, String?>) -> CommandResult =
        { arguments, commandEnvironment ->
            executeProcess(
                arguments = arguments,
                environment = commandEnvironment,
                workingDirectory = workingDirectory,
            )
        },
) : TeamCityBuildArtifactClient,
    TeamCityRunClient {
    /** Reads build and build-type identity through `teamcity run view`. */
    override fun readBuild(
        buildId: Long,
    ): TeamCityBuild {
        val root =
            executeJson(
                "run",
                "view",
                buildId.toString(),
                "--json",
            )
        val buildType =
            root["buildType"]?.jsonObject
                ?: throw IllegalArgumentException("TeamCity build JSON is missing 'buildType'.")

        return TeamCityBuild(
            id = root.requiredString("id").toLong(),
            state = root.requiredString("state"),
            status = root.requiredString("status"),
            branchName = root.requiredString("branchName"),
            buildTypeName = buildType.requiredString("name"),
            webUrl = root["webUrl"]?.jsonPrimitive?.content,
        )
    }

    /** Lists matching runs through the non-interactive TeamCity CLI JSON contract. */
    override fun listRuns(
        buildTypeId: String,
        branch: String,
        status: String,
        limit: Int,
    ): List<TeamCityRun> {
        require(limit > 0) { "TeamCity run list limit must be positive." }
        val root =
            executeJson(
                "run",
                "list",
                "--job",
                buildTypeId,
                "--branch",
                branch,
                "--status",
                status,
                "--limit",
                limit.toString(),
                "--json",
            )
        return root["build"]?.jsonArray.orEmpty().map { it.jsonObject.toTeamCityRun() }
    }

    /** Queues a run through the TeamCity CLI. */
    override fun startRun(
        buildTypeId: String,
        branch: String,
    ): Result<TeamCityRun, TeamCityRunStartError> =
        executeStartRun(
            "run",
            "start",
            buildTypeId,
            "--branch",
            branch,
            "--json",
        ).andThen { root ->
            try {
                Ok(root.toTeamCityRun())
            } catch (
                _: RuntimeException,
            ) {
                Err(TeamCityRunStartError.InvalidResponse)
            }
        }

    /** Waits for a run through the CLI with validated polling and timeout bounds. */
    override fun watchRun(
        buildId: Long,
        pollIntervalSeconds: Int,
        timeoutMinutes: Int,
    ): TeamCityRun {
        require(pollIntervalSeconds in 1..300) {
            "TeamCity polling interval must be between 1 and 300 seconds."
        }
        require(timeoutMinutes in 1..1440) {
            "TeamCity timeout must be between 1 and 1440 minutes."
        }
        return executeJson(
            "run",
            "watch",
            buildId.toString(),
            "--interval",
            pollIntervalSeconds.toString(),
            "--timeout",
            "${timeoutMinutes}m",
            "--json",
        ).toTeamCityRun()
    }

    /** Reads the current run state through `teamcity run view`. */
    override fun readRun(
        buildId: Long,
    ): TeamCityRun =
        executeJson(
            "run",
            "view",
            buildId.toString(),
            "--json",
        ).toTeamCityRun()

    /** Downloads every artifact published by [buildId] into [outputDirectory]. */
    override fun downloadArtifacts(
        buildId: Long,
        outputDirectory: File,
    ) {
        outputDirectory.mkdirs()
        executeTeamCity(
            "run",
            "download",
            buildId.toString(),
            "--output",
            outputDirectory.absolutePath,
        )
    }

    private fun executeTeamCity(
        vararg arguments: String,
    ): CommandResult {
        val command =
            listOf(
                "teamcity",
                "--no-color",
                "--no-input",
            ) + arguments
        val result =
            execute(
                command,
                environment,
            )
        require(result.exitCode == 0) {
            "TeamCity CLI failed with exit code ${result.exitCode}: ${result.error.trim()}"
        }
        return result
    }

    private fun executeJson(
        vararg arguments: String,
    ): JsonObject {
        val result =
            executeTeamCity(
                arguments = arguments,
            )
        val root =
            runCatching { Json.parseToJsonElement(result.output).jsonObject }
                .getOrElse { error ->
                    throw IllegalArgumentException(
                        "TeamCity CLI returned invalid JSON.",
                        error,
                    )
                }
        root["error"]?.jsonObject?.let { error ->
            val message = error["message"]?.jsonPrimitive?.content ?: "unknown TeamCity CLI error"
            throw IllegalArgumentException("TeamCity CLI failed: $message")
        }
        return root
    }

    private fun executeStartRun(
        vararg arguments: String,
    ): Result<JsonObject, TeamCityRunStartError> {
        val command =
            listOf(
                "teamcity",
                "--no-color",
                "--no-input",
            ) + arguments
        val result =
            try {
                execute(
                    command,
                    environment,
                )
            } catch (
                exception: InterruptedException,
            ) {
                Thread.currentThread().interrupt()
                throw exception
            } catch (
                exception: IOException,
            ) {
                return Err(
                    TeamCityRunStartError.Unavailable(
                        detail = exception.message.orEmpty(),
                    ),
                )
            }
        if (result.exitCode != 0) {
            return Err(
                TeamCityRunStartError.CommandFailed(
                    exitCode = result.exitCode,
                    detail = result.error.take(MAX_ERROR_DETAIL_LENGTH),
                ),
            )
        }
        val root =
            try {
                Json.parseToJsonElement(result.output).jsonObject
            } catch (
                _: RuntimeException,
            ) {
                return Err(TeamCityRunStartError.InvalidResponse)
            }
        root["error"]?.jsonObject?.let { error ->
            return Err(
                TeamCityRunStartError.CommandFailed(
                    exitCode = result.exitCode,
                    detail =
                        error["message"]
                            ?.jsonPrimitive
                            ?.content
                            .orEmpty()
                            .take(MAX_ERROR_DETAIL_LENGTH),
                ),
            )
        }
        return Ok(root)
    }

    /**
     * Captured process result supplied by the injectable CLI execution boundary.
     *
     * @property exitCode operating-system process exit code.
     * @property output normalized standard output.
     * @property error normalized standard error.
     */
    data class CommandResult(
        val exitCode: Int,
        val output: String,
        val error: String,
    )

    private companion object {
        const val MAX_ERROR_DETAIL_LENGTH = 500

        fun executeProcess(
            arguments: List<String>,
            environment: Map<String, String?>,
            workingDirectory: File?,
        ): CommandResult {
            val processBuilder = ProcessBuilder(arguments)
            workingDirectory?.let(processBuilder::directory)
            environment.forEach { (name, value) ->
                if (value == null) {
                    processBuilder.environment().remove(name)
                } else {
                    processBuilder.environment()[name] = value
                }
            }
            val process = processBuilder.start()
            val output = ByteArrayOutputStream()
            val error = ByteArrayOutputStream()
            process.inputStream.use { input -> input.copyTo(output) }
            process.errorStream.use { input -> input.copyTo(error) }
            return CommandResult(
                exitCode = process.waitFor(),
                output = output.toString().trim(),
                error = error.toString().trim(),
            )
        }
    }
}

private fun JsonObject.requiredString(
    name: String,
): String =
    this[name]?.jsonPrimitive?.content
        ?: throw IllegalArgumentException("TeamCity build JSON is missing '$name'.")

private fun JsonObject.toTeamCityRun(): TeamCityRun =
    TeamCityRun(
        id =
            requiredString(
                name = "id",
            ).toLong(),
        state =
            requiredString(
                name = "state",
            ),
        status = this["status"]?.jsonPrimitive?.content,
        statusText = this["statusText"]?.jsonPrimitive?.content,
        branchName = this["branchName"]?.jsonPrimitive?.content,
        webUrl = this["webUrl"]?.jsonPrimitive?.content,
    )
