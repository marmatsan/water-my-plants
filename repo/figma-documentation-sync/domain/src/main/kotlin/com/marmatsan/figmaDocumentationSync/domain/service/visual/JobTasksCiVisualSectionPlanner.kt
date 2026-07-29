package com.marmatsan.figmaDocumentationSync.domain.service.visual

import com.marmatsan.figmaDocumentationSync.domain.model.visual.CiVisualPlan

/** Plans executable phases and outcomes for every effective CI job. */
internal class JobTasksCiVisualSectionPlanner : CiVisualSectionPlanner {
    /** Builds the effective CI job tasks and outcomes section. */
    override fun create(
        context: CiVisualPlanningContext
    ): CiVisualPlan.Section {
        val config = context.config
        val nodes =
            listOf(
                context.ciPipeline,
                context.figmaPipeline
            ).flatMap { pipeline -> jobsInDependencyOrder(pipeline).map { job -> pipeline to job } }
                .filter { (_, job) -> visualPhases(job).isNotEmpty() || visualOutcomes(job).isNotEmpty() }
                .mapIndexed { index, (pipeline, job) ->
                    jobTaskNode(
                        id = "job-tasks-${pipeline.id}-${job.id}",
                        pipeline = pipeline,
                        job = job,
                        row = 0,
                        column = index,
                        config = config
                    )
                }
        return visualSection(
            target = "ci.jobTasks",
            name = "Job Tasks",
            description = "Ordered TeamCity phases, Gradle tasks, decisions, and outcomes for every CI job.",
            sources =
                listOf(
                    config.teamCitySource,
                    config.visualContractSource
                ),
            orientation = CiVisualPlan.Orientation.GRID,
            nodes = nodes,
            connections = emptyList(),
            config = config
        )
    }
}
