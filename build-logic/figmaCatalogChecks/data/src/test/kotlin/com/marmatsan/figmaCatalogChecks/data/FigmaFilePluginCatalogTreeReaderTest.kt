package com.marmatsan.figmaCatalogChecks.data

import com.marmatsan.figmaCatalogChecks.domain.CatalogVersion
import com.marmatsan.figmaCatalogChecks.domain.PluginCatalogNode
import com.marmatsan.figmaCatalogChecks.domain.PluginCatalogTree
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.JsonPrimitive

internal class FigmaFilePluginCatalogTreeReaderTest : FunSpec({

    test("readSection maps Figma plugin tree nodes connected by connector endpoints") {
        // GIVEN
        val section = FigmaNode(
            id = "63069:594",
            name = "version catalog: plugins",
            type = "SECTION",
            children = listOf(
                FigmaNode(
                    id = "63069:595",
                    name = "com",
                    type = "SECTION",
                    children = listOf(
                        pluginTreeNode(
                            id = "root",
                            pluginId = "com",
                            showVersion = false,
                            x = 100.0,
                            y = 100.0
                        ),
                        pluginTreeNode(
                            id = "android",
                            pluginId = "android",
                            showVersion = false,
                            x = 100.0,
                            y = 300.0
                        ),
                        pluginTreeNode(
                            id = "library",
                            pluginId = "library",
                            version = "androidGradlePlugin",
                            showVersion = true,
                            appliedToModules = listOf(
                                "core\\core_ui",
                                "onboarding\\onboarding_ui"
                            ),
                            x = 100.0,
                            y = 500.0
                        ),
                        connector(
                            id = "root-android",
                            parentId = "root",
                            childId = "android"
                        ),
                        connector(
                            id = "android-library",
                            parentId = "android",
                            childId = "library"
                        )
                    )
                )
            )
        )

        // WHEN
        val actualTree = FigmaFilePluginCatalogTreeReader().readSection(
            section = section,
            sectionNodeId = "63069:594"
        )

        // THEN
        actualTree shouldBe PluginCatalogTree(
            roots = listOf(
                PluginCatalogNode(
                    id = "com",
                    children = listOf(
                        PluginCatalogNode(
                            id = "android",
                            children = listOf(
                                PluginCatalogNode(
                                    id = "library",
                                    version = CatalogVersion("androidGradlePlugin"),
                                    appliedToModules = listOf(
                                        "core\\core_ui",
                                        "onboarding\\onboarding_ui"
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    }
})

private fun pluginTreeNode(
    id: String,
    pluginId: String,
    version: String = "Plugin version",
    showVersion: Boolean,
    appliedToModules: List<String> = emptyList(),
    x: Double,
    y: Double
): FigmaNode =
    FigmaNode(
        id = id,
        name = ".tree Node",
        type = "INSTANCE",
        absoluteBoundingBox = FigmaRectangle(
            x = x,
            y = y,
            width = 100.0,
            height = 100.0
        ),
        componentProperties = mapOf(
            "Type" to FigmaComponentProperty(
                type = "VARIANT",
                value = JsonPrimitive("Plugin")
            ),
            "Plugin ID#1345:16" to FigmaComponentProperty(
                type = "TEXT",
                value = JsonPrimitive(pluginId)
            ),
            "Plugin version#63081:2" to FigmaComponentProperty(
                type = "TEXT",
                value = JsonPrimitive(version)
            ),
            "Show plugin version#58719:0" to FigmaComponentProperty(
                type = "BOOLEAN",
                value = JsonPrimitive(showVersion)
            )
        ),
        children = appliedToModules.map(::module)
    )

private fun module(name: String): FigmaNode =
    FigmaNode(
        id = "$name-module",
        name = ".module",
        type = "INSTANCE",
        children = listOf(
            FigmaNode(
                id = "$name-text",
                name = "name",
                type = "TEXT",
                characters = name
            )
        )
    )

private fun connector(
    id: String,
    parentId: String,
    childId: String
): FigmaNode =
    FigmaNode(
        id = id,
        name = "simple-simple",
        type = "CONNECTOR",
        connectorStart = FigmaConnectorEndpoint(endpointNodeId = parentId),
        connectorEnd = FigmaConnectorEndpoint(endpointNodeId = childId)
    )
