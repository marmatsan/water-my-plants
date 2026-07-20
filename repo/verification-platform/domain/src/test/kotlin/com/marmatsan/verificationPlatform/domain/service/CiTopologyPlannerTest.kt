package com.marmatsan.verificationPlatform.domain.service

import com.marmatsan.verificationPlatform.domain.model.CiExecutionLane
import com.marmatsan.verificationPlatform.domain.model.CiPlan
import com.marmatsan.verificationPlatform.domain.model.CiTopologyMode
import com.marmatsan.verificationPlatform.domain.model.RepositoryChangeSet
import com.marmatsan.verificationPlatform.domain.model.VerificationUnitId
import com.marmatsan.verificationPlatform.testModuleGraph
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class CiTopologyPlannerTest : FunSpec({
    test("two agents preview supplemental and Gradle work in parallel after documentation") {
        val topology = CiTopologyPlanner().create(
            plan(".teamcity/settings.kts", "repo/figma-documentation-sync/tools/package.json"),
            availableAgents = 2
        )

        topology.mode shouldBe CiTopologyMode.MULTI_AGENT_PARALLEL
        topology.lanes.map(CiExecutionLane::id) shouldBe listOf(
            "documentation",
            "supplemental-verification",
            "gradle-verification",
            "ci-gate"
        )
        topology.lane("supplemental-verification").needs shouldBe listOf("documentation")
        topology.lane("gradle-verification").needs shouldBe listOf("documentation")
        topology.lane("ci-gate").needs shouldBe listOf(
            "documentation",
            "supplemental-verification",
            "gradle-verification"
        )
        topology.authoritativeStatusPublisherLaneId shouldBe "ci-gate"
    }

    test("empty future lanes are omitted for documentation-only work") {
        val topology = CiTopologyPlanner().create(plan("docs/README.md"), availableAgents = 3)

        topology.lanes.map(CiExecutionLane::id) shouldBe listOf(
            "documentation",
            "repository-verification",
            "ci-gate"
        )
        topology.lane("repository-verification").verificationUnits shouldBe
            listOf(VerificationUnitId.REPOSITORY_DIFF)
    }

    test("zero available agents is rejected") {
        shouldThrow<IllegalArgumentException> {
            CiTopologyPlanner().create(plan("docs/README.md"), availableAgents = 0)
        }.message shouldBe "CI topology requires at least one available agent."
    }
}) {
    companion object {
        private fun plan(vararg paths: String): CiPlan = CiPlanFactory().create(
            changeSet = RepositoryChangeSet(
                comparisonBase = "base-sha",
                head = "head-sha",
                changedFiles = paths.toList()
            ),
            moduleGraph = testModuleGraph()
        )

        private fun CiPlan.requiredUnitIds() =
            verificationUnits.filter { unit -> unit.required }.map { unit -> unit.id }

        private fun com.marmatsan.verificationPlatform.domain.model.CiExecutionTopology.lane(id: String) =
            lanes.single { lane -> lane.id == id }
    }
}
