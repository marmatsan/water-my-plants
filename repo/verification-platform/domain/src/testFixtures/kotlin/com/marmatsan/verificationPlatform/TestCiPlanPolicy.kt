package com.marmatsan.verificationPlatform

import com.marmatsan.verificationPlatform.domain.model.ci.CiPlanPolicy

fun testCiPlanPolicy(): CiPlanPolicy =
    CiPlanPolicy(
        toolingPathPrefixes = listOf("tooling/"),
        buildInfrastructurePathPrefixes = listOf("build-infrastructure/"),
        buildInfrastructurePaths =
            setOf(
                "settings.gradle.kts",
                "build.gradle.kts",
                "gradle.properties"
            ),
        portableDistributionPathPrefixes =
            listOf(
                "build-infrastructure/public-api/",
                "tooling/public-api/"
            ),
        portableDistributionPaths =
            setOf(
                "build-infrastructure/settings.gradle.kts",
                "tooling/settings.gradle.kts"
            ),
        toolingCapabilities =
            listOf(
                "java",
                "node"
            ),
        buildInfrastructureCapabilities = listOf("java"),
        portableDistributionCapabilities =
            listOf(
                "java",
                "node"
            ),
        buildInfrastructureVerificationTasks =
            listOf(
                "checkBuildInfrastructure",
                "checkVersionOwnership"
            ),
        portableDistributionVerificationTasks = listOf("verifyPortableDistribution"),
        targetedModuleSupplementalTasks = listOf("checkSharedUsage")
    )
