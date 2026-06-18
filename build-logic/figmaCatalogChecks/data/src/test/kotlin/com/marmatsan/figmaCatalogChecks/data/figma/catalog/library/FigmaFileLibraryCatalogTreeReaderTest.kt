package com.marmatsan.figmaCatalogChecks.data.figma.catalog.library

import com.marmatsan.figmaCatalogChecks.data.figma.dto.FigmaComponentProperty
import com.marmatsan.figmaCatalogChecks.data.figma.dto.FigmaConnectorEndpoint
import com.marmatsan.figmaCatalogChecks.data.figma.dto.FigmaNode
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.CatalogVersion
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.LibraryCatalogEntry
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.LibraryCatalogNode
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.LibraryCatalogTree
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.JsonPrimitive

internal class FigmaFileLibraryCatalogTreeReaderTest : FunSpec({

    test("readSection maps Figma library tree nodes connected by connector endpoints") {
        // GIVEN
        val section = FigmaNode(
            id = "63069:629",
            name = "version catalog: libs",
            type = "SECTION",
            children = listOf(
                FigmaNode(
                    id = "63069:630",
                    name = "androidx",
                    type = "SECTION",
                    children = listOf(
                        libraryTreeNode(
                            id = "root",
                            group = "androidx",
                            showArtifacts = false
                        ),
                        libraryTreeNode(
                            id = "compose",
                            group = "compose",
                            entries = listOf(
                                artifact(
                                    name = "compose-bom",
                                    version = "2026.05.01",
                                    showVersion = true
                                ),
                                artifactsBundle(
                                    alias = "composeBundle",
                                    artifacts = listOf(
                                        "ui",
                                        "ui-tooling"
                                    ),
                                    version = "version",
                                    withVersion = false,
                                    requiredByModules = listOf(":build-logic:compose")
                                )
                            )
                        ),
                        connector(
                            id = "root-compose",
                            parentId = "root",
                            childId = "compose"
                        )
                    )
                )
            )
        )

        // WHEN
        val actualTree = figmaFileLibraryCatalogTreeReader().readSection(
            section = section,
            sectionNodeId = "63069:629"
        )

        // THEN
        actualTree shouldBe LibraryCatalogTree(
            roots = listOf(
                LibraryCatalogNode(
                    group = "androidx",
                    artifactsVisible = false,
                    children = listOf(
                        LibraryCatalogNode(
                            group = "compose",
                            entries = listOf(
                                LibraryCatalogEntry.Artifact(
                                    artifact = "compose-bom",
                                    version = CatalogVersion("2026.05.01")
                                ),
                                LibraryCatalogEntry.ArtifactsBundle(
                                    alias = "composeBundle",
                                    artifacts = listOf(
                                        "ui",
                                        "ui-tooling"
                                    ),
                                    version = CatalogVersion(null),
                                    requiredByModules = listOf(":build-logic:compose")
                                )
                            )
                        )
                    )
                )
            )
        )
    }

    test("readSection uses rendered artifact version when component property keeps placeholder") {
        // GIVEN
        val section = FigmaNode(
            id = "63069:629",
            name = "version catalog: libs",
            type = "SECTION",
            children = listOf(
                FigmaNode(
                    id = "63069:630",
                    name = "androidx",
                    type = "SECTION",
                    children = listOf(
                        libraryTreeNode(
                            id = "activity",
                            group = "activity",
                            entries = listOf(
                                artifact(
                                    name = "activity-compose",
                                    version = "artifact version",
                                    renderedVersion = "activityComposeVersion",
                                    showVersion = true
                                )
                            )
                        )
                    )
                )
            )
        )

        // WHEN
        val actualTree = figmaFileLibraryCatalogTreeReader().readSection(
            section = section,
            sectionNodeId = "63069:629"
        )

        // THEN
        val artifactEntry = actualTree.roots
            .single()
            .entries
            .single() as LibraryCatalogEntry.Artifact

        artifactEntry.version shouldBe CatalogVersion("activityComposeVersion")
    }
})

private fun figmaFileLibraryCatalogTreeReader(): FigmaFileLibraryCatalogTreeReader =
    FigmaFileLibraryCatalogTreeReader(
        nodeReader = FigmaLibraryCatalogNodeReader(
            entryReader = FigmaLibraryCatalogEntryReader()
        )
    )

private fun libraryTreeNode(
    id: String,
    group: String,
    showArtifacts: Boolean = true,
    entries: List<FigmaNode> = emptyList()
): FigmaNode =
    FigmaNode(
        id = id,
        name = ".tree Node",
        type = "INSTANCE",
        componentProperties = mapOf(
            "Type" to FigmaComponentProperty(
                type = "VARIANT",
                value = JsonPrimitive("Library")
            ),
            "Library group#1345:12" to FigmaComponentProperty(
                type = "TEXT",
                value = JsonPrimitive(group)
            ),
            "Show artifacts#63079:0" to FigmaComponentProperty(
                type = "BOOLEAN",
                value = JsonPrimitive(showArtifacts)
            )
        ),
        children = listOf(
            FigmaNode(
                id = "$id-artifacts",
                name = "artifacts",
                type = "FRAME",
                children = entries
            )
        )
    )

private fun artifact(
    name: String,
    version: String,
    renderedVersion: String = version,
    showVersion: Boolean,
    requiredByModules: List<String> = emptyList()
): FigmaNode =
    FigmaNode(
        id = "$name-artifact",
        name = ".artifact",
        type = "INSTANCE",
        componentId = "63069:700",
        componentProperties = mapOf(
            "Artifact name#1345:6" to FigmaComponentProperty(
                type = "TEXT",
                value = JsonPrimitive(name)
            ),
            "Artifact version#63080:1" to FigmaComponentProperty(
                type = "TEXT",
                value = JsonPrimitive(version)
            ),
            "Show version#63090:0" to FigmaComponentProperty(
                type = "BOOLEAN",
                value = JsonPrimitive(showVersion)
            )
        ),
        children = listOf(
            FigmaNode(
                id = "$name-content",
                name = "content",
                type = "FRAME",
                children = listOfNotNull(
                    FigmaNode(
                        id = "$name-name",
                        name = "artifact name",
                        type = "TEXT",
                        characters = name,
                        componentPropertyReferences = mapOf("characters" to "Artifact name#1345:6")
                    ),
                    if (showVersion) {
                        FigmaNode(
                            id = "$name-separator",
                            name = ":",
                            type = "TEXT",
                            characters = ":"
                        )
                    } else {
                        null
                    },
                    if (showVersion) {
                        FigmaNode(
                            id = "$name-version",
                            name = "artifact version",
                            type = "TEXT",
                            characters = renderedVersion
                        )
                    } else {
                        null
                    }
                )
            )
        ) + requiredByModules.map(::module)
    )

private fun artifactsBundle(
    alias: String,
    artifacts: List<String>,
    version: String,
    withVersion: Boolean,
    requiredByModules: List<String> = emptyList()
): FigmaNode =
    FigmaNode(
        id = "$alias-bundle",
        name = ".artifacts bundle",
        type = "INSTANCE",
        componentId = "63069:714",
        componentProperties = mapOf(
            "Alias#63081:0" to FigmaComponentProperty(
                type = "TEXT",
                value = JsonPrimitive(alias)
            ),
            "Version#63081:1" to FigmaComponentProperty(
                type = "TEXT",
                value = JsonPrimitive(version)
            ),
            "With version#58712:0" to FigmaComponentProperty(
                type = "BOOLEAN",
                value = JsonPrimitive(withVersion)
            )
        ),
        children = listOf(
            FigmaNode(
                id = "$alias-artifacts",
                name = "artifacts",
                type = "FRAME",
                children = artifacts.map { artifactName ->
                    artifact(
                        name = artifactName,
                        version = "artifact version",
                        showVersion = false
                    )
                }
            )
        ) + requiredByModules.map(::module)
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
