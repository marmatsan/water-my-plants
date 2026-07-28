package com.marmatsan.dependencies.catalog.api

import com.marmatsan.unitTest.dsl.given
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class LibraryAliasTest :
    FunSpec(
        {
            test("builds stable aliases for overlapping and independent artifact names") {
                given {
                    listOf(
                        AliasFixture(
                            group = "androidx.compose",
                            artifact = "compose-bom",
                        ) to "androidx.compose.bom",
                        AliasFixture(
                            group = "androidx.activity",
                            artifact = "activity-compose",
                        ) to "androidx.activity.compose",
                        AliasFixture(
                            group = "org.junit.jupiter",
                            artifact = "junit-jupiter-api",
                        ) to "org.junit.jupiter.api",
                        AliasFixture(
                            group = "com.google.protobuf",
                            artifact = "protoc",
                        ) to "com.google.protobuf.protoc",
                    )
                }.whenever { fixtures ->
                    fixtures.map { (coordinate, expectedAlias) ->
                        libraryAlias(
                            libraryGroup = coordinate.group,
                            artifact = coordinate.artifact,
                        ) to expectedAlias
                    }
                }.then { aliases ->
                    aliases.forEach { (actual, expected) ->
                        actual shouldBe expected
                    }
                }
            }
        },
    )

private data class AliasFixture(
    val group: String,
    val artifact: String,
)
