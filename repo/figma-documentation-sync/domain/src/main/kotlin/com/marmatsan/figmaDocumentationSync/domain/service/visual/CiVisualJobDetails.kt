package com.marmatsan.figmaDocumentationSync.domain.service.visual

import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiJob
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiPipeline
import com.marmatsan.figmaDocumentationSync.domain.model.visual.CiVisualPlan
import com.marmatsan.figmaDocumentationSync.domain.model.visual.CiVisualPlanConfig

internal fun jobsInDependencyOrder(
    pipeline: CiPipeline,
): List<CiJob> {
    val remaining = pipeline.jobs.toMutableList()
    val ordered = mutableListOf<CiJob>()
    val pipelineJobIds = pipeline.jobs.map(CiJob::id).toSet()
    while (remaining.isNotEmpty()) {
        val completedIds = ordered.map(CiJob::id).toSet()
        val next =
            remaining.firstOrNull { job ->
                job.dependencies
                    .map(CiJob.Dependency::jobId)
                    .filter { dependencyId -> dependencyId in pipelineJobIds }
                    .all { dependencyId -> dependencyId in completedIds }
            } ?: return pipeline.jobs
        ordered += next
        remaining -= next
    }
    return ordered
}

internal fun jobTaskNode(
    id: String,
    pipeline: CiPipeline,
    job: CiJob,
    row: Int,
    column: Int,
    config: CiVisualPlanConfig,
): CiVisualPlan.Node =
    visualNode(
        id = id,
        type = CiVisualPlan.Type.JOB,
        environment = CiVisualPlan.Environment.TEAMCITY,
        name = job.name,
        description = "${jobDescription(job.name)} TeamCity pipeline: ${pipeline.name}.",
        source = config.teamCitySource,
        row = row,
        column = column,
        config = config,
    ).copy(
        phases =
            visualPhases(
                job = job,
            ),
        outcomes =
            visualOutcomes(
                job = job,
            ),
    )

internal fun visualPhases(
    job: CiJob,
): List<CiVisualPlan.Phase> =
    job.steps.mapIndexed { phaseIndex, step ->
        val order =
            formattedOrder(
                value = phaseIndex + 1,
            )
        val gradleTasks =
            gradleTaskNames(
                command = step.command,
            )
        CiVisualPlan.Phase(
            order = order,
            title = step.name,
            technicalId = step.id,
            description =
                phaseDescription(
                    command = step.command,
                    hasGradleTasks = gradleTasks.isNotEmpty(),
                ),
            steps =
                gradleTasks
                    .flatMap(::gradleTaskSpecs)
                    .mapIndexed { stepIndex, spec -> spec.toVisualStep("$order.${stepIndex + 1}") },
        )
    }

internal fun visualOutcomes(
    job: CiJob,
): List<CiVisualPlan.Outcome> {
    var nextOrder = job.steps.size + 1
    val artifacts =
        job.artifacts
            .filter(CiJob.Artifact::publish)
            .map { artifact ->
                CiVisualPlan.Outcome(
                    order =
                        formattedOrder(
                            value = nextOrder++,
                        ),
                    kind = CiVisualPlan.OutcomeKind.ARTIFACT,
                    title = "Publish build artifact",
                    technicalId = artifact.path,
                    description = "Makes the job output available to later CI jobs.",
                    condition = "After successful job execution",
                )
            }
    val checks =
        job.publishedChecks.map { check ->
            CiVisualPlan.Outcome(
                order =
                    formattedOrder(
                        value = nextOrder++,
                    ),
                kind = CiVisualPlan.OutcomeKind.CHECK,
                title = "Publish GitHub check",
                technicalId = check.name,
                description = "Reports the verified job result to the pull request.",
                condition = "After successful job execution",
            )
        }
    return artifacts + checks
}

internal fun postMergeCheckOutcomes(): List<CiVisualPlan.Outcome> =
    listOf(
        CiVisualPlan.Outcome(
            order = "01",
            kind = CiVisualPlan.OutcomeKind.SUCCESS,
            title = "Metadata matches",
            technicalId = "modelHash · writerHash · fingerprints",
            description = "Confirms that the canonical model and visual writer state are current.",
            condition = "Canonical metadata matches",
        ),
        CiVisualPlan.Outcome(
            order = "02",
            kind = CiVisualPlan.OutcomeKind.ACTION,
            title = "Visual sync required",
            technicalId = "modelHash · writerHash · fingerprints",
            description = "Hands control to the supervised visual synchronization loop.",
            condition = "Canonical metadata differs",
        ),
    )

private fun formattedOrder(
    value: Int,
): String =
    value.toString().padStart(
        length = 2,
        padChar = '0',
    )

private fun phaseDescription(
    command: String,
    hasGradleTasks: Boolean,
): String =
    when {
        hasGradleTasks -> "Runs the Gradle entry points owned by this TeamCity phase."
        "test-agent-capabilities.ps1" in command -> "Validates the build agent before repository work begins."
        "teamcity-configs:generate" in command -> "Generates the effective TeamCity configuration with Maven."
        else -> "Runs the repository-owned command for this TeamCity phase."
    }

private fun gradleTaskSpecs(
    task: String,
): List<StepSpec> =
    when (task) {
        "prepareTeamCityCiPlan" -> {
            listOf(
                actionSpec(
                    task = task,
                    description = "Prepares the reviewed verification plan consumed by TeamCity.",
                ),
                actionSpec(
                    task = "generateCiPlan",
                    description = "Calculates the affected verification scope.",
                    condition = "Gradle dependency of prepareTeamCityCiPlan",
                ),
            )
        }

        "%ci.plan.gradleTasks%" -> {
            dynamicCiPlanStepSpecs
        }

        "check" -> {
            listOf(
                actionSpec(
                    task = task,
                    description =
                        "Runs the repository verification lifecycle, including Kotlin style, catalogs, " +
                            "versions, CI freshness, and verification-platform checks.",
                ),
            )
        }

        "classifyCanonicalFigmaSyncChangeImpact" -> {
            listOf(
                actionSpec(
                    task = task,
                    description = "Classifies whether the canonical Figma sync needs full verification.",
                ),
                actionSpec(
                    task = "cleanCanonicalFigmaSyncReports",
                    description = "Removes stale canonical Figma sync reports.",
                    condition = "Gradle dependency of classifyCanonicalFigmaSyncChangeImpact",
                ),
            )
        }

        "materializeFigmaSyncCiConfiguration",
        "generateCanonicalFigmaSyncModel",
        "checkCanonicalFigmaTrunkSync",
        -> {
            listOf(
                actionSpec(
                    task = task,
                    description = "Runs only for a validated full Figma verification scope.",
                    condition = "Full Figma verification",
                ),
            )
        }

        "prepareCanonicalFigmaSync" -> {
            listOf(
                actionSpec(
                    task = task,
                    description = "Builds the MCP runners and target-scoped visual plan.",
                ),
                actionSpec(
                    task = "writeFigmaWriterProjectConfig",
                    description = "Projects the repository-specific Figma writer contract.",
                    condition = "Gradle dependency of prepareCanonicalFigmaSync",
                ),
            )
        }

        else -> {
            listOf(
                actionSpec(
                    task = task,
                    description = "Runs this repository-owned Gradle entry point.",
                ),
            )
        }
    }

private fun StepSpec.toVisualStep(
    order: String,
) = CiVisualPlan.Step(
    order = order,
    role = role,
    title = title,
    technicalId = technicalId,
    description = description,
    condition = condition,
)

private fun gradleTaskNames(
    command: String,
): List<String> =
    command
        .lineSequence()
        .mapNotNull { line -> gradleInvocation.find(line.trim())?.groupValues?.get(1) }
        .flatMap { arguments ->
            arguments
                .trim()
                .split(Regex("\\s+"))
                .takeWhile { argument -> !argument.startsWith("-") }
                .asSequence()
        }.toList()

private fun actionSpec(
    task: String,
    description: String,
    condition: String? = null,
) = StepSpec(
    role = CiVisualPlan.StepRole.ACTION,
    title =
        gradleTaskTitle(
            task = task,
        ),
    technicalId = task,
    description = description,
    condition = condition,
)

private fun groupSpec(
    title: String,
    technicalId: String,
    description: String,
    condition: String,
) = StepSpec(
    role = CiVisualPlan.StepRole.GROUP,
    title = title,
    technicalId = technicalId,
    description = description,
    condition = condition,
)

private fun gradleTaskTitle(
    task: String,
): String =
    when (task) {
        ":<affected-module>:check" -> {
            "Check affected module"
        }

        "verification-platform:check" -> {
            "Check verification platform"
        }

        else -> {
            task
                .substringAfterLast(':')
                .replace(
                    Regex("([a-z0-9])([A-Z])"),
                    "${'$'}1 ${'$'}2",
                ).replaceFirstChar(Char::uppercaseChar)
        }
    }

private data class StepSpec(
    val role: CiVisualPlan.StepRole,
    val title: String,
    val technicalId: String?,
    val description: String?,
    val condition: String?,
)

private val gradleInvocation =
    Regex(
        """(?:^|\s)(?:call\s+)?(?:\.\\|\./)?gradlew(?:\.bat)?\s+(.+)$""",
        RegexOption.IGNORE_CASE,
    )

private val dynamicCiPlanStepSpecs =
    listOf(
        StepSpec(
            role = CiVisualPlan.StepRole.DECISION,
            title = "Select verification tasks",
            technicalId = "ci.plan.gradleTasks",
            description = "Expands the reviewed CI plan into the tasks for this change.",
            condition = "Repository change scope",
        ),
        groupSpec(
            title = "Always",
            technicalId = "checkGitWorkflow · checkDocumentation",
            description = "Validates the Git workflow and repository documentation contracts.",
            condition = "Always",
        ),
        groupSpec(
            title = "According to changes",
            technicalId =
                "checkRepositoryDiff · checkTeamCityDsl · :<affected-module>:check · " +
                    "checkFigmaCatalogUsage",
            description = "Runs only the repository, TeamCity, module, and catalog checks selected by impact.",
            condition = "Repository change scope",
        ),
        groupSpec(
            title = "Full verification",
            technicalId = "check",
            description =
                "Includes Kotlin style, catalog usage and naming, CI freshness, and " +
                    "verification-platform checks.",
            condition = "TeamCity changes or fail-closed fallback",
        ),
    )
