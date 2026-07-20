package com.marmatsan.verificationPlatform.data.json

import com.marmatsan.verificationPlatform.domain.model.RepositoryChangeSet
import com.marmatsan.verificationPlatform.domain.service.CiPlanFactory
import com.marmatsan.verificationPlatform.testModuleGraph
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class CiPlanJsonTest :
    FunSpec(
        {
            test("round trips the versioned verification contract") {
                val expected =
                    CiPlanFactory().create(
                        changeSet =
                            RepositoryChangeSet(
                                comparisonBase = "base-sha",
                                head = "head-sha",
                                changedFiles = listOf(".teamcity/settings.kts"),
                            ),
                        moduleGraph = testModuleGraph(),
                    )
                val json = CiPlanJson()

                json.read(
                    kotlinx.serialization.json.Json
                        .encodeToString(expected),
                ) shouldBe expected
            }
        },
    )
