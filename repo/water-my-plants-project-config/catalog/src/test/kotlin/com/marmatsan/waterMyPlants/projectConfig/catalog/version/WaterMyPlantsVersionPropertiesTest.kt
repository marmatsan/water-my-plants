package com.marmatsan.waterMyPlants.projectConfig.catalog.version

import com.marmatsan.unitTest.dsl.given
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe

internal class WaterMyPlantsVersionPropertiesTest :
    FunSpec(
        {
            test("version properties read the standalone product registry") {
                given {
                    tempdir(
                        prefix = "water-my-plants-version-properties"
                    ).apply {
                        resolve("versions.properties").writeText("exampleVersion=1.2.3")
                    }
                }.whenever { rootDirectory ->
                    WaterMyPlantsVersionProperties
                        .load(
                            rootDirectory = rootDirectory
                        ).required(
                            key = "exampleVersion"
                        )
                }.then { version ->
                    version shouldBe "1.2.3"
                }
            }

            test("version properties prefer the repository composition registry") {
                given {
                    tempdir(
                        prefix = "water-my-plants-version-precedence"
                    ).apply {
                        resolve("versions.properties").writeText("exampleVersion=standalone")
                        resolve("repo/water-my-plants-project-config")
                            .apply { mkdirs() }
                            .resolve("versions.properties")
                            .writeText("exampleVersion=repository")
                    }
                }.whenever { rootDirectory ->
                    WaterMyPlantsVersionProperties
                        .load(
                            rootDirectory = rootDirectory
                        ).required(
                            key = "exampleVersion"
                        )
                }.then { version ->
                    version shouldBe "repository"
                }
            }

            test("version properties report a missing exact key through the shared resolver") {
                given {
                    tempdir(
                        prefix = "water-my-plants-version-key"
                    ).apply {
                        resolve("versions.properties").writeText("knownVersion=1.0.0")
                    }
                }.whenever { rootDirectory ->
                    shouldThrow<IllegalStateException> {
                        WaterMyPlantsVersionProperties
                            .load(
                                rootDirectory = rootDirectory
                            ).required(
                                key = "missingVersion"
                            )
                    }
                }.then { failure ->
                    failure.message?.contains("Missing version property 'missingVersion'") shouldBe true
                }
            }
        }
    )
