package com.marmatsan.figmaDocumentationSync.domain.model.catalog

import com.marmatsan.unitTest.dsl.given
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class CatalogTreesTest :
    FunSpec(
        {
            test("catalog version is visible by default when it has a value") {
                given {
                    "2.4.0"
                }.whenever { value ->
                    CatalogVersion(
                        value = value
                    )
                }.then { version ->
                    version.visible shouldBe true
                }
            }

            test("catalog version is hidden by default when it has no value") {
                given<String?> {
                    null
                }.whenever { value ->
                    CatalogVersion(
                        value = value
                    )
                }.then { version ->
                    version.visible shouldBe false
                }
            }

            test("catalog version cannot be visible without a value") {
                given<String?> {
                    null
                }.whenever { value ->
                    shouldThrow<IllegalArgumentException> {
                        CatalogVersion(
                            value = value,
                            visible = true
                        )
                    }
                }.then { exception ->
                    exception.message shouldBe "A catalog version cannot be visible when its value is null"
                }
            }

            test("library node shows artifacts by default when it has entries") {
                given {
                    listOf(
                        LibraryCatalogEntry.Artifact(
                            artifact = "activity-compose",
                            version =
                                CatalogVersion(
                                    value = "1.12.0"
                                )
                        )
                    )
                }.whenever { entries ->
                    LibraryCatalogNode(
                        group = "activity",
                        entries = entries
                    )
                }.then { node ->
                    node.artifactsVisible shouldBe true
                }
            }
        }
    )
