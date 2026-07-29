package com.marmatsan.dependencies.catalog.api

import com.marmatsan.unitTest.dsl.given
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class DependencyCatalogTest :
    FunSpec(
        {
            test("library catalog nodes reject compact paths") {
                given {
                    "org.jetbrains"
                }.whenever { compactPath ->
                    shouldThrow<IllegalArgumentException> {
                        LibraryCatalogNode(
                            group = compactPath
                        )
                    }
                }.then { failure ->
                    failure.message shouldBe
                        "Library group 'org.jetbrains' must be one non-blank path segment without dots or whitespace"
                }
            }

            test("plugin catalog nodes reject compact paths") {
                given {
                    "org.jetbrains"
                }.whenever { compactPath ->
                    shouldThrow<IllegalArgumentException> {
                        PluginCatalogNode(
                            id = compactPath
                        )
                    }
                }.then { failure ->
                    failure.message shouldBe
                        "Plugin id 'org.jetbrains' must be one non-blank path segment without dots or whitespace"
                }
            }

            test("library bundle aliases require the Bundle suffix") {
                given {
                    "composeUi"
                }.whenever { alias ->
                    shouldThrow<IllegalArgumentException> {
                        LibraryCatalogEntry.Bundle(
                            alias = alias,
                            artifacts = listOf("ui"),
                            version = null
                        )
                    }
                }.then { failure ->
                    failure.message shouldBe
                        "Library bundle alias 'composeUi' must end in Bundle"
                }
            }
        }
    )
