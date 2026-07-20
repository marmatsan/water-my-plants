package com.marmatsan.figmaDocumentationSync.data.yaml.ci

import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiConnection
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiNode
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import java.time.LocalDate

internal class CiExternalTopologyYamlReaderTest : FunSpec(
    {

    test("read maps versioned nodes connections and validation metadata") {
        // GIVEN
        val file = Files.createTempFile(
            "external-topology",
            ".yaml"
        ).toFile().apply {
            writeText(
                """
                schemaVersion: 1
                validation:
                  lastValidatedOn: "2026-07-14"
                  warnAfterDays: 90
                nodes:
                  - id: operator
                    type: actor
                    name: Operator
                    description: Starts the manual flow.
                  - id: teamcity-server
                    type: system
                    name: TeamCity Server
                    description: Orchestrates CI.
                connections:
                  - id: operator-teamcity
                    source: operator
                    target: teamcity-server
                    label: Start build
                    description: Requests a build through TeamCity.
                    protocol: HTTPS
                    authentication:
                      - TeamCity access token
                    policy: Service Auth
                    automation: manual
                    annotation: The interface may be the UI or CLI.
                """.trimIndent()
            )
        }

        // WHEN
        val topology = CiExternalTopologyYamlReader().read(file)

        // THEN
        topology.schemaVersion shouldBe 1
        topology.validation.lastValidatedOn shouldBe LocalDate.of(
            2026,
            7,
            14
        )
        topology.validation.warnAfterDays shouldBe 90
        topology.nodes.map(CiNode::id) shouldBe listOf(
            "operator",
            "teamcity-server"
        )
        topology.connections.single() shouldBe CiConnection(
            id = "operator-teamcity",
            sourceNodeId = "operator",
            targetNodeId = "teamcity-server",
            label = "Start build",
            description = "Requests a build through TeamCity.",
            protocol = "HTTPS",
            authentication = listOf("TeamCity access token"),
            policy = "Service Auth",
            path = null,
            automation = CiConnection.Automation.Manual,
            annotation = "The interface may be the UI or CLI."
        )
    }

    test("read rejects connections to unknown nodes") {
        // GIVEN
        val file = Files.createTempFile(
            "invalid-external-topology",
            ".yaml"
        ).toFile().apply {
            writeText(
                """
                schemaVersion: 1
                validation:
                  lastValidatedOn: "2026-07-14"
                  warnAfterDays: 90
                nodes:
                  - id: operator
                    type: actor
                    name: Operator
                    description: Starts the manual flow.
                connections:
                  - id: missing-target
                    source: operator
                    target: teamcity-server
                    label: Start build
                    description: Invalid endpoint.
                    automation: manual
                """.trimIndent()
            )
        }

        // WHEN / THEN
        shouldThrow<IllegalArgumentException> {
            CiExternalTopologyYamlReader().read(file)
        }.message shouldBe "Unknown CI connection target 'teamcity-server'"
    }
}
)
