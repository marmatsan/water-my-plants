package com.marmatsan.figmaCatalogChecks.data.figma.modules

import com.marmatsan.figmaCatalogChecks.data.figma.dto.FigmaConnectorEndpoint
import com.marmatsan.figmaCatalogChecks.data.figma.dto.FigmaNode
import com.marmatsan.figmaCatalogChecks.domain.model.modules.ModuleDependency
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class FigmaModuleDependenciesReaderTest : FunSpec({

    test("readSection maps connectors from dependent module to dependency module") {
        // GIVEN
        val section = FigmaNode(
            id = "section",
            name = "Water My Plants",
            type = "SECTION",
            children = listOf(
                module(id = "app", moduleName = ":app"),
                module(id = "core-ui", moduleName = ":core:ui"),
                FigmaNode(
                    id = "connector",
                    name = "connector",
                    type = "CONNECTOR",
                    connectorStart = FigmaConnectorEndpoint(endpointNodeId = "app"),
                    connectorEnd = FigmaConnectorEndpoint(endpointNodeId = "core-ui")
                )
            )
        )

        // WHEN
        val dependencies = FigmaModuleDependenciesReader().readSection(section)

        // THEN
        dependencies shouldBe setOf(
            ModuleDependency(
                dependentModule = ":app",
                dependencyModule = ":core:ui"
            )
        )
    }
})

private fun module(
    id: String,
    moduleName: String
): FigmaNode =
    FigmaNode(
        id = id,
        name = ".module",
        type = "INSTANCE",
        children = listOf(
            FigmaNode(
                id = "$id-text",
                name = "name",
                type = "TEXT",
                characters = moduleName
            )
        )
    )
