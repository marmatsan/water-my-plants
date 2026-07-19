package com.marmatsan.figmaDocumentationSync.domain.model.ci

/** Effective CI pipeline supplied by a project-selected adapter. */
data class CiPipeline(
    val id: String,
    val name: String,
    val triggers: List<CiTrigger>,
    val jobs: List<CiJob>
)
