package com.marmatsan.verificationPlatform.data.json

import com.marmatsan.verificationPlatform.domain.model.RepositoryChangeSet
import com.marmatsan.verificationPlatform.domain.service.CiPlanFactory
import com.marmatsan.verificationPlatform.domain.service.CiTopologyPlanner
import com.marmatsan.verificationPlatform.testModuleGraph
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class CiExecutionTopologyJsonTest :
    FunSpec(
        {
            test("round trips the preview topology contract") {
                val plan =
                    CiPlanFactory().create(
                        changeSet =
                            RepositoryChangeSet(
                                comparisonBase = "base-sha",
                                head = "head-sha",
                                changedFiles = listOf(".teamcity/settings.kts"),
                            ),
                        moduleGraph = testModuleGraph(),
                    )
                val expected =
                    CiTopologyPlanner().create(
                        plan = plan,
                        availableAgents = 3,
                    )
                val json = CiExecutionTopologyJson()
                val output =
                    kotlin.io.path
                        .createTempFile()
                        .toFile()

                json.write(
                    expected,
                    output,
                )

                json.read(output.readText()) shouldBe expected
            }
        },
    )
