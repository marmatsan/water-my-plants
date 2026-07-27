package com.marmatsan.figmaDocumentationSync.data.yaml.ci

import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiWindowsRuntime
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import java.time.LocalDate

internal class CiWindowsRuntimeYamlReaderTest :
    FunSpec(
        {
            val temporaryDirectory =
                tempdir(
                    prefix = "ci-windows-runtime-yaml",
                )

            test("read maps the versioned Windows service runtime") {
                val file =
                    temporaryDirectory
                        .resolve("windows-runtime.yaml")
                        .apply {
                            writeText(
                                """
                                schemaVersion: 1
                                validation:
                                  lastValidatedOn: "2026-07-16"
                                  warnAfterDays: 90
                                platform: Windows
                                services:
                                  - id: teamcity-server
                                    name: TeamCity Server
                                    description: Hosts TeamCity.
                                    service: TeamCity
                                    startup: Automatic
                                    identity: NT SERVICE\TeamCity
                                """.trimIndent(),
                            )
                        }

                val runtime = CiWindowsRuntimeYamlReader().read(file)

                runtime.schemaVersion shouldBe 1
                runtime.validation.lastValidatedOn shouldBe
                    LocalDate.of(
                        2026,
                        7,
                        16,
                    )
                runtime.validation.warnAfterDays shouldBe 90
                runtime.platform shouldBe "Windows"
                runtime.services.single() shouldBe
                    CiWindowsRuntime.Service(
                        id = "teamcity-server",
                        name = "TeamCity Server",
                        description = "Hosts TeamCity.",
                        service = "TeamCity",
                        startup = "Automatic",
                        identity = "NT SERVICE\\TeamCity",
                    )
            }

            test("read rejects duplicate service ids") {
                val file =
                    temporaryDirectory
                        .resolve("invalid-windows-runtime.yaml")
                        .apply {
                            writeText(
                                """
                                schemaVersion: 1
                                validation:
                                  lastValidatedOn: "2026-07-16"
                                  warnAfterDays: 90
                                platform: Windows
                                services:
                                  - id: teamcity
                                    name: TeamCity Server
                                    description: Hosts TeamCity.
                                    service: TeamCity
                                    startup: Automatic
                                    identity: LocalSystem
                                  - id: teamcity
                                    name: TeamCity Agent
                                    description: Runs builds.
                                    service: TCBuildAgent
                                    startup: Automatic
                                    identity: LocalSystem
                                """.trimIndent(),
                            )
                        }

                shouldThrow<IllegalArgumentException> {
                    CiWindowsRuntimeYamlReader().read(file)
                }.message shouldBe "CI Windows runtime service ids must be unique"
            }
        },
    )
