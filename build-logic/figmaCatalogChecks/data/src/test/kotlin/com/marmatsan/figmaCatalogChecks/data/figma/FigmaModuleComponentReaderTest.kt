package com.marmatsan.figmaCatalogChecks.data.figma

import com.marmatsan.figmaCatalogChecks.domain.model.*

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class FigmaModuleComponentReaderTest : FunSpec({

    test("readComponent returns distinct module names from component variants") {
        // GIVEN
        val component = FigmaNode(
            id = "63085:793",
            name = ".module",
            type = "COMPONENT_SET",
            children = listOf(
                FigmaNode(
                    id = "1:1",
                    name = "name=:app, size=small",
                    type = "COMPONENT"
                ),
                FigmaNode(
                    id = "1:2",
                    name = "name=:app, size=big",
                    type = "COMPONENT"
                ),
                FigmaNode(
                    id = "1:3",
                    name = "name=:build-logic:android, size=small",
                    type = "COMPONENT"
                )
            )
        )

        // WHEN
        val modules = FigmaModuleComponentReader().readComponent(
            component = component,
            componentNodeId = "63085:793"
        )

        // THEN
        modules shouldBe setOf(":app", ":build-logic:android")
    }

    test("readComponent fails when no module variant exists") {
        // GIVEN
        val component = FigmaNode(
            id = "63085:793",
            name = ".module",
            type = "COMPONENT_SET"
        )

        // WHEN / THEN
        shouldThrow<IllegalStateException> {
            FigmaModuleComponentReader().readComponent(
                component = component,
                componentNodeId = "63085:793"
            )
        }
    }
})
