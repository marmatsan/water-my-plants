package com.marmatsan.ci.data.json

import com.marmatsan.ci.domain.model.RepositoryChangeSet
import com.marmatsan.ci.domain.service.CiPlanFactory
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class CiPlanJsonTest : FunSpec({
    test("round trips the versioned verification contract") {
        val expected = CiPlanFactory().create(
            RepositoryChangeSet(
                comparisonBase = "base-sha",
                head = "head-sha",
                changedFiles = listOf(".teamcity/settings.kts")
            )
        )
        val json = CiPlanJson()

        json.read(kotlinx.serialization.json.Json.encodeToString(expected)) shouldBe expected
    }
})
