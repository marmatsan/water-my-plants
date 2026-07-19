package com.marmatsan.ci.domain.service

import com.marmatsan.ci.domain.model.CiPlan
import com.marmatsan.ci.domain.model.CiPlanMode
import com.marmatsan.ci.domain.model.CiScope
import com.marmatsan.ci.domain.model.RepositoryChangeSet
import com.marmatsan.ci.domain.model.RepositoryModuleGraph
import com.marmatsan.ci.domain.model.VerificationUnit
import com.marmatsan.ci.domain.model.VerificationUnitId

/** Pure classifier that turns repository paths into provider-neutral verification units. */
class CiPlanFactory {
    fun create(changeSet: RepositoryChangeSet, moduleGraph: RepositoryModuleGraph): CiPlan {
        val changedFiles = changeSet.changedFiles.map(::normalize).distinct().sorted()
        val moduleImpactAnalyzer = ModuleImpactAnalyzer()
        val moduleImpact = moduleImpactAnalyzer.analyze(
            changedFiles = changedFiles.filterNot(::isDocumentation),
            graph = moduleGraph
        )
        val categories = changedFiles.map { path ->
            category(path, moduleGraph, moduleImpactAnalyzer)
        }.toSet()
        val documentationOnly = changedFiles.isNotEmpty() && categories == setOf(PathCategory.DOCUMENTATION)
        val moduleGraphInvalid = changedFiles.any { path -> !isDocumentation(path) } && !moduleImpact.isValid
        val unknown = changedFiles.isEmpty() || PathCategory.UNKNOWN in categories || moduleGraphInvalid
        val scope = scope(categories, documentationOnly, unknown)
        val targetedModuleVerification =
            !unknown &&
                PathCategory.APPLICATION in categories &&
                categories.all { category ->
                    category == PathCategory.APPLICATION || category == PathCategory.DOCUMENTATION
                } &&
                moduleImpact.affectedModules.isNotEmpty()
        val fullVerification = !documentationOnly && !targetedModuleVerification
        val fallbackReason = when {
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
            verificationUnits = units(
                categories = categories,
                documentationOnly = documentationOnly,
                targetedModuleVerification = targetedModuleVerification,
                fullVerification = fullVerification,
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
        affectedModules: List<String>,
        fallbackReason: String?
    ): List<VerificationUnit> = listOf(
        unit(
            id = VerificationUnitId.DOCUMENTATION,
            required = true,
            capabilities = listOf("powershell"),
            reasons = listOf("Documentation structure and coverage are repository-wide invariants.")
        ),
        unit(
            id = VerificationUnitId.REPOSITORY_DIFF,
            required = documentationOnly,
            needs = listOf(VerificationUnitId.DOCUMENTATION),
            capabilities = listOf("git"),
            reasons = requiredReasons(documentationOnly, "Every changed path is documentation-only.")
        ),
        unit(
            id = VerificationUnitId.TEAMCITY_DSL,
            required = PathCategory.TEAMCITY in categories,
            needs = listOf(VerificationUnitId.DOCUMENTATION),
            capabilities = listOf("java", "maven-wrapper"),
            reasons = requiredReasons(PathCategory.TEAMCITY in categories, "TeamCity configuration changed.")
        ),
        unit(
            id = VerificationUnitId.FIGMA_TOOLING,
            required = PathCategory.FIGMA in categories,
            needs = listOf(VerificationUnitId.DOCUMENTATION),
            capabilities = listOf("java", "android-sdk", "node"),
            reasons = requiredReasons(PathCategory.FIGMA in categories, "Figma Design Sync implementation changed.")
        ),
        unit(
            id = VerificationUnitId.DEPENDENCY_CATALOG,
            required = PathCategory.DEPENDENCY_INFRASTRUCTURE in categories,
            needs = listOf(VerificationUnitId.DOCUMENTATION),
            capabilities = listOf("java", "android-sdk"),
            reasons = requiredReasons(
                PathCategory.DEPENDENCY_INFRASTRUCTURE in categories,
                "Dependency catalog or Gradle infrastructure changed."
            )
        ),
        unit(
            id = VerificationUnitId.GRADLE_VERIFICATION,
            required = !documentationOnly,
            needs = listOf(VerificationUnitId.DOCUMENTATION),
            capabilities = listOf("java", "android-sdk"),
            gradleTasks = when {
                documentationOnly -> emptyList()
                targetedModuleVerification ->
                    affectedModules.map { module -> "$module:check" } + CHECK_FIGMA_CATALOG_USAGE
                else -> listOf("check")
            },
            reasons = when {
                fallbackReason != null -> listOf(fallbackReason)
                targetedModuleVerification -> listOf(
                    "Changed modules and their transitive reverse dependents can be verified independently."
                )
                fullVerification -> listOf("Every non-documentation change retains full Gradle verification.")
                else -> emptyList()
            }
        ),
        unit(
            id = VerificationUnitId.PUBLISH_REPORTS,
            required = true,
            needs = listOf(
                VerificationUnitId.DOCUMENTATION,
                VerificationUnitId.REPOSITORY_DIFF,
                VerificationUnitId.TEAMCITY_DSL,
                VerificationUnitId.FIGMA_TOOLING,
                VerificationUnitId.DEPENDENCY_CATALOG,
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

    private fun requiredReasons(required: Boolean, reason: String): List<String> =
        if (required) listOf(reason) else emptyList()

    private fun category(
        path: String,
        moduleGraph: RepositoryModuleGraph,
        moduleImpactAnalyzer: ModuleImpactAnalyzer
    ): PathCategory = when {
        isDocumentation(path) -> PathCategory.DOCUMENTATION
        path.startsWith(".teamcity/") -> PathCategory.TEAMCITY
        path.startsWith("repo/figma-design-sync/") -> PathCategory.FIGMA
        path.startsWith("repo/dependency-catalog/") ||
            path.startsWith("repo/gradle-plugins/") ||
            path in ROOT_GRADLE_FILES -> PathCategory.DEPENDENCY_INFRASTRUCTURE
        moduleImpactAnalyzer.moduleFor(path, moduleGraph) != null -> PathCategory.APPLICATION
        else -> PathCategory.UNKNOWN
    }

    private fun scope(
        categories: Set<PathCategory>,
        documentationOnly: Boolean,
        unknown: Boolean
    ): CiScope = when {
        documentationOnly -> CiScope.DOCUMENTATION_ONLY
        unknown -> CiScope.UNKNOWN
        categories.size > 1 -> CiScope.MIXED
        PathCategory.TEAMCITY in categories -> CiScope.TEAMCITY
        PathCategory.FIGMA in categories -> CiScope.FIGMA_TOOLING
        PathCategory.DEPENDENCY_INFRASTRUCTURE in categories -> CiScope.DEPENDENCY_INFRASTRUCTURE
        PathCategory.APPLICATION in categories -> CiScope.APPLICATION
        else -> CiScope.UNKNOWN
    }

    private fun isDocumentation(path: String): Boolean {
        if (!path.endsWith(".md", ignoreCase = true)) return false

        val segments = path.split('/')
        return path.equals("README.md", ignoreCase = true) ||
            path.equals("AGENTS.md", ignoreCase = true) ||
            path.equals(".teamcity/README.md", ignoreCase = true) ||
            path.endsWith("/AGENTS.md", ignoreCase = true) ||
            segments.any { segment -> segment.equals("docs", ignoreCase = true) } ||
            (
                segments.size == 3 &&
                    segments.first().equals("repo", ignoreCase = true) &&
                    segments.last().equals("README.md", ignoreCase = true)
                )
    }

    private fun normalize(path: String): String = path.trim().replace('\\', '/')

    private enum class PathCategory {
        DOCUMENTATION,
        TEAMCITY,
        FIGMA,
        DEPENDENCY_INFRASTRUCTURE,
        APPLICATION,
        UNKNOWN
    }

    private companion object {
        const val SCHEMA_VERSION = 1
        const val CHECK_FIGMA_CATALOG_USAGE = "checkFigmaCatalogUsage"
        val ROOT_GRADLE_FILES = setOf("settings.gradle.kts", "build.gradle.kts", "gradle.properties")
    }
}
