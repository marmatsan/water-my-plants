package com.marmatsan.figmaDocumentationSync.domain.model.ci

/**
 * Effective CI pipeline supplied by a project-selected adapter.
 *
 * @property id stable adapter-independent pipeline identity.
 * @property name human-readable pipeline name.
 * @property triggers conditions that start the pipeline.
 * @property jobs effective jobs executed by the pipeline.
 */
data class CiPipeline(
    val id: String,
    val name: String,
    val triggers: List<CiTrigger>,
    val jobs: List<CiJob>
)
