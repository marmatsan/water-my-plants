package com.marmatsan.figmaDocumentationSync.domain.model.ci

/**
 * Effective CI configuration used as the visual model input.
 *
 * Concrete CI adapters translate their generated configuration into this
 * portable representation before it reaches the design-model generator.
 */
data class CiConfiguration(
    val pipelines: List<CiPipeline>,
    val vcsRoots: List<CiVcsRoot>,
) {
    init {
        require(
            pipelines
                .map(
                    transform = CiPipeline::id,
                ).let { ids -> ids.size == ids.toSet().size },
        ) {
            "CI pipeline ids must be unique"
        }
        require(
            vcsRoots
                .map(
                    transform = CiVcsRoot::id,
                ).let { ids -> ids.size == ids.toSet().size },
        ) {
            "CI VCS root ids must be unique"
        }
    }
}
