package com.marmatsan.figmaDocumentationSync.data.properties.versions

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files

internal class VersionsPropertiesReaderTest : FunSpec(
    {

    test("readSections keeps version groups in file order") {
        // GIVEN
        val versionsFile = Files.createTempFile(
            "versions",
            ".properties"
        ).toFile()
        versionsFile.writeText(
            """
            ## Main project dependencies
            # com.android.tools.build:gradle
            androidGradlePluginVersion=9.2.1
            ## Libraries
            # io.kotest:kotest-runner-junit5
            kotestLibraryVersion=6.2.1
            ## Plugins
            figmaCodeConnectPluginVersion=1.4.0
            """.trimIndent()
        )
        val reader = VersionsPropertiesReader()

        // WHEN
        val sections = reader.readSections(versionsFile)

        // THEN
        sections.map { section -> section.name } shouldBe listOf(
            "Main project dependencies",
            "Libraries",
            "Plugins"
        )
        sections[1].versions shouldBe mapOf("kotestLibraryVersion" to "6.2.1")
    }
}
)
