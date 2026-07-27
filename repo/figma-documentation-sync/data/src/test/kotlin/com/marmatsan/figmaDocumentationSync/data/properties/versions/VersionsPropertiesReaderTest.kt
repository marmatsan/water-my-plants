package com.marmatsan.figmaDocumentationSync.data.properties.versions

import com.marmatsan.unitTest.dsl.given
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe

internal class VersionsPropertiesReaderTest :
    FunSpec(
        {
            val temporaryDirectory =
                tempdir(
                    prefix = "versions-properties-reader",
                )

            test("readSections keeps version groups in file order") {
                given {
                    temporaryDirectory.resolve("versions.properties").apply {
                        writeText(
                            """
                            ## Main project dependencies
                            # com.android.tools.build:gradle
                            androidGradlePluginVersion=9.2.1
                            ## Libraries
                            # io.kotest:kotest-runner-junit5
                            kotestLibraryVersion=6.2.1
                            ## Plugins
                            figmaCodeConnectPluginVersion=1.4.0
                            """.trimIndent(),
                        )
                    }
                }.whenever { versionsFile ->
                    VersionsPropertiesReader().readSections(
                        file = versionsFile,
                    )
                }.then { sections ->
                    sections.map { section -> section.name } shouldBe
                        listOf(
                            "Main project dependencies",
                            "Libraries",
                            "Plugins",
                        )
                    sections[1].versions shouldBe mapOf("kotestLibraryVersion" to "6.2.1")
                }
            }
        },
    )
