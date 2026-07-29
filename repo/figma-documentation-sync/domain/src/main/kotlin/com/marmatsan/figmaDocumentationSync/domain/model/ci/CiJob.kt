package com.marmatsan.figmaDocumentationSync.domain.model.ci

/**
 * Effective CI job and the operational details rendered in Figma.
 *
 * @property id stable adapter-independent job identity.
 * @property name human-readable job name.
 * @property steps ordered executable job steps.
 * @property repositoryIds VCS roots consumed by the job.
 * @property artifacts job artifact declarations.
 * @property dependencies upstream job and artifact requirements.
 * @property publishedChecks external status checks published by the job.
 */
data class CiJob(
    val id: String,
    val name: String,
    val steps: List<Step>,
    val repositoryIds: List<String>,
    val artifacts: List<Artifact>,
    val dependencies: List<Dependency>,
    val publishedChecks: List<PublishedCheck>
) {
    /**
     * One executable step in an effective CI job.
     *
     * @property id stable step identity within the job.
     * @property name human-readable step name.
     * @property command normalized command or task description.
     */
    data class Step(
        val id: String,
        val name: String,
        val command: String
    )

    /**
     * Artifact produced or consumed by a CI job.
     *
     * @property path artifact path or pattern.
     * @property publish whether the job publishes the artifact externally.
     * @property shareWithJobs whether downstream jobs may consume the artifact.
     */
    data class Artifact(
        val path: String,
        val publish: Boolean,
        val shareWithJobs: Boolean
    )

    /**
     * Dependency on an upstream job and selected artifacts.
     *
     * @property jobId stable upstream job id.
     * @property artifactPaths artifact paths required from the upstream job.
     */
    data class Dependency(
        val jobId: String,
        val artifactPaths: List<String>
    )

    /**
     * Repository status check published by the job.
     *
     * @property name externally visible check name.
     */
    data class PublishedCheck(
        val name: String
    )
}
