package com.marmatsan.figmaDocumentationSync.domain.model.ci

/** Effective CI job and the operational details rendered in Figma. */
data class CiJob(
    val id: String,
    val name: String,
    val steps: List<Step>,
    val repositoryIds: List<String>,
    val artifacts: List<Artifact>,
    val dependencies: List<Dependency>,
    val publishedChecks: List<PublishedCheck>,
) {
    data class Step(
        val id: String,
        val name: String,
        val command: String,
    )

    data class Artifact(
        val path: String,
        val publish: Boolean,
        val shareWithJobs: Boolean,
    )

    data class Dependency(
        val jobId: String,
        val artifactPaths: List<String>,
    )

    data class PublishedCheck(
        val name: String,
    )
}
