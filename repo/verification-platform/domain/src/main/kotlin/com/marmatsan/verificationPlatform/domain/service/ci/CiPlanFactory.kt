package com.marmatsan.verificationPlatform.domain.service.ci

import com.marmatsan.verificationPlatform.domain.model.ci.CiPlan
import com.marmatsan.verificationPlatform.domain.model.ci.CiPlanMode
import com.marmatsan.verificationPlatform.domain.model.ci.CiPlanPolicy
import com.marmatsan.verificationPlatform.domain.model.ci.CiScope
import com.marmatsan.verificationPlatform.domain.model.ci.VerificationUnit
import com.marmatsan.verificationPlatform.domain.model.ci.VerificationUnitId
import com.marmatsan.verificationPlatform.domain.model.git.RepositoryChangeSet
import com.marmatsan.verificationPlatform.domain.model.modules.RepositoryModuleGraph
import com.marmatsan.verificationPlatform.domain.service.modules.ModuleImpactAnalyzer

/**
 * Selects provider-neutral verification for one committed repository change.
 *
 * The factory applies the fail-closed behavior documented by
 * `ci-verification-plan.feature`: unknown paths or an invalid module graph keep
 * full repository verification, while safely classified application changes
 * may select affected module tasks.
 */
class CiPlanFactory(
    private val policy: CiPlanPolicy = CiPlanPolicy()
) {
    /**
     * Creates the authoritative verification plan for [changeSet].
     *
     * @param changeSet committed paths and revisions being compared.
     * @param moduleGraph modules and dependency edges used to calculate impact.
     * @return a provider-neutral plan whose required units can be consumed by a
     * CI adapter.
     */
    fun create(
        changeSet: RepositoryChangeSet,
        moduleGraph: RepositoryModuleGraph
    ): CiPlan {
        val changedFiles =
            changeSet.changedFiles
                .map(
                    transform = ::normalize
                ).distinct()
                .sorted()
        val moduleImpactAnalyzer = ModuleImpactAnalyzer()
        val moduleImpact =
            moduleImpactAnalyzer.analyze(
                changedFiles = changedFiles.filterNot(::isDocumentation),
                graph = moduleGraph
            )
        val categories =
            changedFiles
                .map { path ->
                    category(
                        path = path,
                        moduleGraph = moduleGraph,
                        moduleImpactAnalyzer = moduleImpactAnalyzer
                    )
                }.toSet()
        val documentationOnly = changedFiles.isNotEmpty() && categories == setOf(PathCategory.DOCUMENTATION)
        val moduleGraphInvalid =
            changedFiles.any { path ->
                !isDocumentation(
                    path = path
                )
            } && !moduleImpact.isValid
        val unknown = changedFiles.isEmpty() || PathCategory.UNKNOWN in categories || moduleGraphInvalid
        val scope =
            scope(
                categories = categories,
                documentationOnly = documentationOnly,
                unknown = unknown
            )
        val targetedModuleVerification =
            !unknown &&
                PathCategory.APPLICATION in categories &&
                categories.all { category ->
                    category == PathCategory.APPLICATION || category == PathCategory.DOCUMENTATION
                } &&
                moduleImpact.affectedModules.isNotEmpty()
        val fullVerification = !documentationOnly && !targetedModuleVerification
        val portableDistribution =
            changedFiles.any { path ->
                !isDocumentation(
                    path = path
                ) &&
                    isPortableDistribution(
                        path = path
                    )
            }
        val fallbackReason =
            when {
                changedFiles.isEmpty() -> "No changed files were resolved; verification fails closed."
                moduleGraphInvalid -> moduleImpact.fallbackReason
                unknown -> "At least one changed path has no targeted verification policy."
                else -> null
            }

        return CiPlan(
            schemaVersion = SCHEMA_VERSION,
            mode = CiPlanMode.ENFORCED,
            comparisonBase = changeSet.comparisonBase,
            head = changeSet.head,
            scope = scope,
            changedFiles = changedFiles,
            changedModules = moduleImpact.changedModules,
            affectedModules = moduleImpact.affectedModules,
            verificationUnits =
                units(
                    categories = categories,
                    documentationOnly = documentationOnly,
                    targetedModuleVerification = targetedModuleVerification,
                    fullVerification = fullVerification,
                    portableDistribution = portableDistribution,
                    affectedModules = moduleImpact.affectedModules,
                    fallbackReason = fallbackReason
                ),
            fullVerification = fullVerification,
            fallbackReason = fallbackReason
        )
    }

    private fun units(
        categories: Set<PathCategory>,
        documentationOnly: Boolean,
        targetedModuleVerification: Boolean,
        fullVerification: Boolean,
        portableDistribution: Boolean,
        affectedModules: List<String>,
        fallbackReason: String?
    ): List<VerificationUnit> =
        listOf(
            unit(
                id = VerificationUnitId.GIT_WORKFLOW,
                required = true,
                capabilities = listOf("git"),
                gradleTasks = listOf(CHECK_GIT_WORKFLOW),
                reasons = listOf("Every checkout must satisfy the trunk-based branch contract.")
            ),
            unit(
                id = VerificationUnitId.DOCUMENTATION,
                required = true,
                needs = listOf(VerificationUnitId.GIT_WORKFLOW),
                capabilities =
                    listOf(
                        "java",
                        "android-sdk",
                        "git"
                    ),
                gradleTasks = listOf(CHECK_DOCUMENTATION),
                reasons = listOf("Documentation structure and coverage are repository-wide invariants.")
            ),
            unit(
                id = VerificationUnitId.REPOSITORY_DIFF,
                required = documentationOnly,
                needs = listOf(VerificationUnitId.DOCUMENTATION),
                capabilities = listOf("git"),
                gradleTasks =
                    requiredTasks(
                        required = documentationOnly,
                        CHECK_REPOSITORY_DIFF
                    ),
                reasons =
                    requiredReasons(
                        required = documentationOnly,
                        reason = "Every changed path is documentation-only."
                    )
            ),
            unit(
                id = VerificationUnitId.TEAMCITY_DSL,
                required = PathCategory.TEAMCITY in categories,
                needs = listOf(VerificationUnitId.DOCUMENTATION),
                capabilities =
                    listOf(
                        "java",
                        "maven-wrapper"
                    ),
                gradleTasks =
                    requiredTasks(
                        required = PathCategory.TEAMCITY in categories,
                        CHECK_TEAMCITY_DSL
                    ),
                reasons =
                    requiredReasons(
                        required = PathCategory.TEAMCITY in categories,
                        reason = "TeamCity configuration changed."
                    )
            ),
            unit(
                id = VerificationUnitId.TOOLING,
                required = PathCategory.TOOLING in categories,
                needs = listOf(VerificationUnitId.DOCUMENTATION),
                capabilities = policy.toolingCapabilities,
                gradleTasks =
                    requiredTasks(
                        required = PathCategory.TOOLING in categories,
                        tasks = policy.toolingVerificationTasks.toTypedArray()
                    ),
                reasons =
                    requiredReasons(
                        required = PathCategory.TOOLING in categories,
                        reason = "A configured repository-tooling surface changed."
                    )
            ),
            unit(
                id = VerificationUnitId.BUILD_INFRASTRUCTURE,
                required = PathCategory.BUILD_INFRASTRUCTURE in categories,
                needs = listOf(VerificationUnitId.DOCUMENTATION),
                capabilities = policy.buildInfrastructureCapabilities,
                gradleTasks =
                    requiredTasks(
                        required = PathCategory.BUILD_INFRASTRUCTURE in categories,
                        tasks = policy.buildInfrastructureVerificationTasks.toTypedArray()
                    ),
                reasons =
                    requiredReasons(
                        required = PathCategory.BUILD_INFRASTRUCTURE in categories,
                        reason = "A configured build-infrastructure surface changed."
                    )
            ),
            unit(
                id = VerificationUnitId.PORTABLE_DISTRIBUTION,
                required = portableDistribution,
                needs =
                    listOf(
                        VerificationUnitId.DOCUMENTATION,
                        VerificationUnitId.BUILD_INFRASTRUCTURE,
                        VerificationUnitId.TOOLING
                    ),
                capabilities = policy.portableDistributionCapabilities,
                gradleTasks =
                    requiredTasks(
                        required = portableDistribution,
                        tasks = policy.portableDistributionVerificationTasks.toTypedArray()
                    ),
                reasons =
                    requiredReasons(
                        required = portableDistribution,
                        reason = "A configured portable-distribution surface changed."
                    )
            ),
            unit(
                id = VerificationUnitId.GRADLE_VERIFICATION,
                required = !documentationOnly,
                needs = listOf(VerificationUnitId.DOCUMENTATION),
                capabilities =
                    listOf(
                        "java",
                        "android-sdk"
                    ),
                gradleTasks =
                    when {
                        documentationOnly -> {
                            emptyList()
                        }

                        targetedModuleVerification -> {
                            affectedModules.map { module -> "$module:check" } +
                                policy.targetedModuleSupplementalTasks
                        }

                        else -> {
                            listOf("check")
                        }
                    },
                reasons =
                    when {
                        fallbackReason != null -> {
                            listOf(fallbackReason)
                        }

                        targetedModuleVerification -> {
                            listOf(
                                "Changed modules and their transitive reverse dependents can be verified independently."
                            )
                        }

                        fullVerification -> {
                            listOf("Every non-documentation change retains full Gradle verification.")
                        }

                        else -> {
                            emptyList()
                        }
                    }
            ),
            unit(
                id = VerificationUnitId.PUBLISH_REPORTS,
                required = true,
                needs =
                    listOf(
                        VerificationUnitId.GIT_WORKFLOW,
                        VerificationUnitId.DOCUMENTATION,
                        VerificationUnitId.REPOSITORY_DIFF,
                        VerificationUnitId.TEAMCITY_DSL,
                        VerificationUnitId.TOOLING,
                        VerificationUnitId.BUILD_INFRASTRUCTURE,
                        VerificationUnitId.PORTABLE_DISTRIBUTION,
                        VerificationUnitId.GRADLE_VERIFICATION
                    ),
                capabilities = emptyList(),
                parallelSafe = false,
                reasons = listOf("The plan and verification evidence must remain inspectable.")
            )
        )

    private fun unit(
        id: VerificationUnitId,
        required: Boolean,
        needs: List<VerificationUnitId> = emptyList(),
        capabilities: List<String>,
        parallelSafe: Boolean = true,
        gradleTasks: List<String> = emptyList(),
        reasons: List<String>
    ) = VerificationUnit(
        id = id,
        required = required,
        needs = needs,
        capabilities = capabilities,
        parallelSafe = parallelSafe,
        gradleTasks = gradleTasks,
        reasons = reasons
    )

    private fun requiredReasons(
        required: Boolean,
        reason: String
    ): List<String> =
        if (required) listOf(reason) else emptyList()

    private fun requiredTasks(
        required: Boolean,
        vararg tasks: String
    ): List<String> =
        if (required) tasks.toList() else emptyList()

    private fun category(
        path: String,
        moduleGraph: RepositoryModuleGraph,
        moduleImpactAnalyzer: ModuleImpactAnalyzer
    ): PathCategory =
        when {
            isDocumentation(
                path = path
            ) -> PathCategory.DOCUMENTATION

            path.startsWith(
                prefix = ".teamcity/"
            ) -> PathCategory.TEAMCITY

            policy.toolingPathPrefixes.any(path::startsWith) -> PathCategory.TOOLING

            policy.buildInfrastructurePathPrefixes.any(path::startsWith) ||
                path in policy.buildInfrastructurePaths -> PathCategory.BUILD_INFRASTRUCTURE

            moduleImpactAnalyzer.moduleFor(
                path = path,
                graph = moduleGraph
            ) != null -> PathCategory.APPLICATION

            else -> PathCategory.UNKNOWN
        }

    private fun scope(
        categories: Set<PathCategory>,
        documentationOnly: Boolean,
        unknown: Boolean
    ): CiScope =
        when {
            documentationOnly -> CiScope.DOCUMENTATION_ONLY
            unknown -> CiScope.UNKNOWN
            categories.size > 1 -> CiScope.MIXED
            PathCategory.TEAMCITY in categories -> CiScope.TEAMCITY
            PathCategory.TOOLING in categories -> CiScope.TOOLING
            PathCategory.BUILD_INFRASTRUCTURE in categories -> CiScope.BUILD_INFRASTRUCTURE
            PathCategory.APPLICATION in categories -> CiScope.APPLICATION
            else -> CiScope.UNKNOWN
        }

    private fun isDocumentation(
        path: String
    ): Boolean {
        if (!path.endsWith(
                ".md",
                ignoreCase = true
            )
        ) {
            return false
        }

        val segments = path.split('/')
        return path.equals(
            "README.md",
            ignoreCase = true
        ) ||
            path.equals(
                "AGENTS.md",
                ignoreCase = true
            ) ||
            path.equals(
                ".teamcity/README.md",
                ignoreCase = true
            ) ||
            path.endsWith(
                "/AGENTS.md",
                ignoreCase = true
            ) ||
            segments.any { segment ->
                segment.equals(
                    "docs",
                    ignoreCase = true
                )
            } ||
            (
                segments.size == 3 &&
                    segments.first().equals(
                        "repo",
                        ignoreCase = true
                    ) &&
                    segments.last().equals(
                        "README.md",
                        ignoreCase = true
                    )
            )
    }

    private fun isPortableDistribution(
        path: String
    ): Boolean =
        policy.portableDistributionPathPrefixes.any(path::startsWith) ||
            path in policy.portableDistributionPaths

    private fun normalize(
        path: String
    ): String =
        path.trim().replace(
            '\\',
            '/'
        )

    private enum class PathCategory {
        DOCUMENTATION,
        TEAMCITY,
        TOOLING,
        BUILD_INFRASTRUCTURE,
        APPLICATION,
        UNKNOWN
    }

    private companion object {
        const val SCHEMA_VERSION = 5
        const val CHECK_GIT_WORKFLOW = "checkGitWorkflow"
        const val CHECK_DOCUMENTATION = "checkDocumentation"
        const val CHECK_REPOSITORY_DIFF = "checkRepositoryDiff"
        const val CHECK_TEAMCITY_DSL = "checkTeamCityDsl"
    }
}
