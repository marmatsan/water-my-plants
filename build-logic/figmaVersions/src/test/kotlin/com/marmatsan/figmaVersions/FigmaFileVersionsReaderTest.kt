package com.marmatsan.figmaVersions

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContainExactly
import kotlinx.serialization.json.JsonPrimitive

internal class FigmaFileVersionsReaderTest : FunSpec({

    test("read returns versions from project version alias and rendered version number") {
        // GIVEN
        val section = FigmaNode(
            id = "62936:183",
            name = "build-logic\\versions.properties",
            type = "SECTION",
            children = listOf(
                FigmaNode(
                    id = "1:2",
                    name = ".project version",
                    type = "INSTANCE",
                    componentId = "63075:591",
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
                    componentId = "63075:591",
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
                ),
                FigmaNode(
                    id = "1:4",
                    name = ".other component",
                    type = "INSTANCE",
                    componentId = "999:999",
                    children = listOf(
                        FigmaNode(
                            id = "1:4:1",
                            name = "ignoredVersion",
                            type = "TEXT",
                            characters = "ignoredVersion"
                        )
                    )
                )
            )
        )

        // WHEN
        val actualVersions = FigmaFileVersionsReader().readSection(
            section = section,
            sectionNodeId = "62936:183",
            versionComponentNodeId = "63075:591"
        )

        // THEN
        actualVersions shouldContainExactly mapOf(
            "kotlinVersion" to "2.4.0",
            "composeBomVersion" to "2026.05.01"
        )
    }
})
