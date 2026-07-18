package com.marmatsan.figmaDesignSync.teamcityAdapter

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files

internal class TeamCityCliClientTest : FunSpec({
    test("reads build identity through the TeamCity CLI JSON contract") {
        val commands = mutableListOf<List<String>>()
        val client = TeamCityCliClient { command ->
            commands += command
            TeamCityCliClient.CommandResult(
                exitCode = 0,
                output =
                    """
                    {
                      "id": "1573",
                      "state": "finished",
                      "status": "SUCCESS",
                      "branchName": "main",
                      "webUrl": "https://teamcity.example/build/1573",
                      "buildType": { "name": "Generate main design model" }
                    }
                    """.trimIndent(),
                error = ""
            )
        }

        val build = client.readBuild(1573)

        build shouldBe TeamCityBuild(
            id = 1573,
            state = "finished",
            status = "SUCCESS",
            branchName = "main",
            buildTypeName = "Generate main design model",
            webUrl = "https://teamcity.example/build/1573"
        )
        commands.single() shouldBe listOf(
            "teamcity",
            "--no-color",
            "--no-input",
            "run",
            "view",
            "1573",
            "--json"
        )
    }

    test("downloads one build artifact set into the requested directory") {
        val output = Files.createTempDirectory("teamcity-cli-download").resolve("artifacts").toFile()
        val commands = mutableListOf<List<String>>()
        val client = TeamCityCliClient { command ->
            commands += command
            TeamCityCliClient.CommandResult(exitCode = 0, output = "downloaded", error = "")
        }

        client.downloadArtifacts(1573, output)

        output.isDirectory shouldBe true
        commands.single() shouldBe listOf(
            "teamcity",
            "--no-color",
            "--no-input",
            "run",
            "download",
            "1573",
            "--output",
            output.absolutePath
        )
        output.parentFile.deleteRecursively()
    }
})
