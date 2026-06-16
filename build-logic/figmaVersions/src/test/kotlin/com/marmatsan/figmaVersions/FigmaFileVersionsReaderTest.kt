package com.marmatsan.figmaVersions

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContainExactly
import kotlinx.serialization.json.JsonPrimitive

internal class FigmaFileVersionsReaderTest : FunSpec({

    test("read returns versions from project version alias and rendered version number") {
        // GIVEN
        val response = FigmaFileResponse(
            document = FigmaNode(
                id = "0:0",
                name = "Document",
                type = "DOCUMENT",
                children = listOf(
                    FigmaNode(
                        id = "1:0",
                        name = "🐘 Gradle dependencies",
                        type = "CANVAS",
                        children = listOf(
                            FigmaNode(
                                id = "1:1",
                                name = "build-logic\\versions.properties",
                                type = "SECTION",
                                children = listOf(
                                    FigmaNode(
                                        id = "1:2",
                                        name = ".project version",
                                        type = "INSTANCE",
                                        componentProperties = mapOf(
                                            "Version alias#63075:0" to FigmaComponentProperty(
                                                type = "TEXT",
                                                value = JsonPrimitive("kotlinVersion")
                                            ),
                                            "Version number#63075:1" to FigmaComponentProperty(
                                                type = "TEXT",
                                                value = JsonPrimitive("kotlinVersion")
                                            )
                                        ),
                                        children = listOf(
                                            FigmaNode(
                                                id = "1:2:1",
                                                name = "kotlinVersion",
                                                type = "TEXT",
                                                characters = "kotlinVersion"
                                            ),
                                            FigmaNode(
                                                id = "1:2:2",
                                                name = "2.4.0",
                                                type = "TEXT",
                                                characters = "2.4.0"
                                            )
                                        )
                                    ),
                                    FigmaNode(
                                        id = "1:3",
                                        name = ".project version",
                                        type = "INSTANCE",
                                        componentProperties = mapOf(
                                            "Version alias#63075:0" to FigmaComponentProperty(
                                                type = "TEXT",
                                                value = JsonPrimitive("composeBomVersion")
                                            ),
                                            "Version number#63075:1" to FigmaComponentProperty(
                                                type = "TEXT",
                                                value = JsonPrimitive("composeBomVersion")
                                            )
                                        ),
                                        children = listOf(
                                            FigmaNode(
                                                id = "1:3:1",
                                                name = "composeBomVersion",
                                                type = "TEXT",
                                                characters = "composeBomVersion"
                                            ),
                                            FigmaNode(
                                                id = "1:3:2",
                                                name = "2026.05.01",
                                                type = "TEXT",
                                                characters = "2026.05.01"
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )

        // WHEN
        val actualVersions = FigmaFileVersionsReader.read(
            response = response,
            pageName = "🐘 Gradle dependencies",
            sectionName = "build-logic\\versions.properties"
        )

        // THEN
        actualVersions shouldContainExactly mapOf(
            "kotlinVersion" to "2.4.0",
            "composeBomVersion" to "2026.05.01"
        )
    }
})
