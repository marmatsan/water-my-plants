package com.marmatsan.verificationPlatform.data.teamcity

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class TeamCityServiceMessageFormatterTest : FunSpec(
    {
    test("escapes TeamCity service message values") {
        TeamCityServiceMessageFormatter().setParameters(
            mapOf("ci.plan.reason" to "unknown | path ['x']\nfull")
        ) shouldBe listOf(
            "##teamcity[setParameter name='ci.plan.reason' value='unknown || path |[|'x|'|]|nfull']"
        )
    }
}
)
