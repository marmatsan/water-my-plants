package com.marmatsan.dependencies.catalog.version

import com.marmatsan.unitTest.dsl.given
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe

internal class PropertiesDependencyVersionResolverTest :
    FunSpec(
        {
            test("resolver loads one consumer-owned properties file") {
                given {
                    tempdir(
                        prefix = "dependency-version-resolver"
                    ).resolve("versions.properties").apply {
                        writeText("exampleVersion=1.2.3")
                    }
                }.whenever { versionsFile ->
                    PropertiesDependencyVersionResolver(
                        source = versionsFile
                    ).resolve(
                        key = "exampleVersion"
                    )
                }.then { version ->
                    version shouldBe "1.2.3"
                }
            }

            test("resolver rejects a source change after its first lookup") {
                given {
                    val directory =
                        tempdir(
                            prefix = "dependency-version-source"
                        )
                    val first =
                        directory.resolve("first.properties").apply {
                            writeText("exampleVersion=1.0.0")
                        }
                    val second =
                        directory.resolve("second.properties").apply {
                            writeText("exampleVersion=2.0.0")
                        }
                    var source = first
                    VersionSourceFixture(
                        resolver =
                            PropertiesDependencyVersionResolver(
                                source = { source }
                            ),
                        selectSecond = { source = second }
                    )
                }.whenever { fixture ->
                    fixture.resolver.resolve("exampleVersion")
                    fixture.selectSecond()
                    shouldThrow<IllegalArgumentException> {
                        fixture.resolver.resolve("exampleVersion")
                    }
                }.then { failure ->
                    failure.message shouldBe
                        "Dependency versions file cannot change after its first value is resolved"
                }
            }

            test("resolver reports a missing exact version key") {
                given {
                    tempdir(
                        prefix = "dependency-version-key"
                    ).resolve("versions.properties").apply {
                        writeText("knownVersion=1.0.0")
                    }
                }.whenever { versionsFile ->
                    shouldThrow<IllegalStateException> {
                        PropertiesDependencyVersionResolver(
                            source = versionsFile
                        ).resolve(
                            key = "missingVersion"
                        )
                    }
                }.then { failure ->
                    failure.message?.contains("Missing version property 'missingVersion'") shouldBe true
                }
            }
        }
    )

private data class VersionSourceFixture(
    val resolver: PropertiesDependencyVersionResolver,
    val selectSecond: () -> Unit
)
