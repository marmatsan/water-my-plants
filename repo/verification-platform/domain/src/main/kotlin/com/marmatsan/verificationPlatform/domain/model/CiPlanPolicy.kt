package com.marmatsan.verificationPlatform.domain.model

/**
 * Repository-specific path policy consumed by the provider-neutral CI planner.
 *
 * The verification platform deliberately owns no concrete repository module
 * names. A consuming composition root supplies the prefixes and exact paths
 * that represent its tooling and portable distribution surfaces.
 *
 * @property toolingPathPrefixes paths classified as repository tooling.
 * @property buildInfrastructurePathPrefixes paths classified as build infrastructure.
 * @property buildInfrastructurePaths exact paths classified as build infrastructure.
 * @property portableDistributionPathPrefixes path prefixes that require staged consumer verification.
 * @property portableDistributionPaths exact paths that require staged consumer verification.
 * @property toolingCapabilities agent capabilities required by repository tooling.
 * @property buildInfrastructureCapabilities agent capabilities required by build infrastructure.
 * @property portableDistributionCapabilities agent capabilities required by staged consumers.
 * @property toolingVerificationTasks reviewed Gradle tasks selected for tooling changes.
 * @property buildInfrastructureVerificationTasks reviewed Gradle tasks selected for build-infrastructure changes.
 * @property portableDistributionVerificationTasks reviewed Gradle tasks selected for portable-distribution changes.
 * @property targetedModuleSupplementalTasks repository-wide tasks added to targeted module verification.
 */
data class CiPlanPolicy(
    val toolingPathPrefixes: List<String> = emptyList(),
    val buildInfrastructurePathPrefixes: List<String> = emptyList(),
    val buildInfrastructurePaths: Set<String> = emptySet(),
    val portableDistributionPathPrefixes: List<String> = emptyList(),
    val portableDistributionPaths: Set<String> = emptySet(),
    val toolingCapabilities: List<String> = emptyList(),
    val buildInfrastructureCapabilities: List<String> = emptyList(),
    val portableDistributionCapabilities: List<String> = emptyList(),
    val toolingVerificationTasks: List<String> = emptyList(),
    val buildInfrastructureVerificationTasks: List<String> = emptyList(),
    val portableDistributionVerificationTasks: List<String> = emptyList(),
    val targetedModuleSupplementalTasks: List<String> = emptyList(),
)
