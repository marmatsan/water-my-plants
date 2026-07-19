package com.marmatsan.ci.data.teamcity

import com.marmatsan.ci.domain.model.RepositoryChangeSet
import com.marmatsan.ci.domain.service.CiPlanFactory
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class TeamCityCiPlanParametersTest : FunSpec({
    test("exports allow-listed parameters for a TeamCity change") {
        val plan = CiPlanFactory().create(
            RepositoryChangeSet(
                comparisonBase = "base-sha",
                head = "head-sha",
                changedFiles = listOf(".teamcity/settings.kts")
            )
        )

        TeamCityCiPlanParameters().create(plan) shouldBe linkedMapOf(
            "ci.plan.schemaVersion" to "1",
            "ci.plan.mode" to "enforced",
            "ci.plan.scope" to "teamcity",
            "ci.plan.comparisonBase" to "base-sha",
            "ci.plan.head" to "head-sha",
            "ci.plan.fullVerification" to "true",
            "ci.plan.fallbackReason" to "",
            "ci.unit.documentation.required" to "true",
            "ci.unit.repository-diff.required" to "false",
            "ci.unit.teamcity-dsl.required" to "true",
            "ci.unit.figma-tooling.required" to "false",
            "ci.unit.dependency-catalog.required" to "false",
            "ci.unit.gradle-verification.required" to "true",
            "ci.unit.publish-reports.required" to "true"
        )
    }
})
