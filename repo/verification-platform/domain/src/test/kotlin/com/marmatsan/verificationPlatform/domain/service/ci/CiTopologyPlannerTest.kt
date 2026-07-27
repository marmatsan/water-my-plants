package com.marmatsan.verificationPlatform.domain.service.ci

import com.marmatsan.verificationPlatform.domain.model.ci.CiExecutionLane
import com.marmatsan.verificationPlatform.domain.model.ci.CiPlan
import com.marmatsan.verificationPlatform.domain.model.ci.CiTopologyMode
import com.marmatsan.verificationPlatform.domain.model.ci.VerificationUnitId
import com.marmatsan.verificationPlatform.domain.model.git.RepositoryChangeSet
import com.marmatsan.verificationPlatform.testCiPlanPolicy
import com.marmatsan.verificationPlatform.testModuleGraph
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class CiTopologyPlannerTest :
    FunSpec(
        {
            test("two agents preview supplemental and Gradle work in parallel after documentation") {
                val topology =
                    CiTopologyPlanner().create(
                        plan =
                            plan(
                                ".teamcity/settings.kts",
                                "tooling/sync/package.json",
                            ),
                        availableAgents = 2,
                    )

                topology.mode shouldBe CiTopologyMode.MULTI_AGENT_PARALLEL
                topology.lanes.map(
                    transform = CiExecutionLane::id,
                ) shouldBe
                    listOf(
                        "documentation",
                        "supplemental-verification",
                        "gradle-verification",
                        "ci-gate",
                    )
                topology
                    .lane(
                        id = "supplemental-verification",
                    ).needs shouldBe listOf("documentation")
                topology
                    .lane(
                        id = "gradle-verification",
                    ).needs shouldBe listOf("documentation")
                topology
                    .lane(
                        id = "ci-gate",
                    ).needs shouldBe
                    listOf(
                        "documentation",
                        "supplemental-verification",
                        "gradle-verification",
                    )
                topology.authoritativeStatusPublisherLaneId shouldBe "ci-gate"
            }

            test("empty future lanes are omitted for documentation-only work") {
                val topology =
                    CiTopologyPlanner().create(
                        plan = plan("docs/README.md"),
                        availableAgents = 3,
                    )

                topology.lanes.map(
                    transform = CiExecutionLane::id,
                ) shouldBe
                    listOf(
                        "documentation",
                        "repository-verification",
                        "ci-gate",
                    )
                topology
                    .lane(
                        id = "repository-verification",
                    ).verificationUnits shouldBe
                    listOf(VerificationUnitId.REPOSITORY_DIFF)
            }

            test("zero available agents is rejected") {
                shouldThrow<IllegalArgumentException> {
                    CiTopologyPlanner().create(
                        plan = plan("docs/README.md"),
                        availableAgents = 0,
                    )
                }.message shouldBe "CI topology requires at least one available agent."
            }
        },
    ) {
    companion object {
        private fun plan(
            vararg paths: String,
        ): CiPlan =
            CiPlanFactory(testCiPlanPolicy()).create(
                changeSet =
                    RepositoryChangeSet(
                        comparisonBase = "base-sha",
                        head = "head-sha",
                        changedFiles = paths.toList(),
                    ),
                moduleGraph = testModuleGraph(),
            )

        private fun CiPlan.requiredUnitIds() =
            verificationUnits.filter { unit -> unit.required }.map { unit -> unit.id }

        private fun com.marmatsan.verificationPlatform.domain.model.ci.CiExecutionTopology.lane(
            id: String,
        ) =
            lanes.single { lane -> lane.id == id }
    }
}
