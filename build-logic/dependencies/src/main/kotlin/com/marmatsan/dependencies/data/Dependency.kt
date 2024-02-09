package com.marmatsan.dependencies.data

sealed class Dependency(
    open val id: String
) {

    data class ArtifactsGroup(
        val name: String,
        val artifacts: List<String>,
        val version: String
    )

    data class Library(
        val group: String,
        val artifactsGroups: List<ArtifactsGroup>? = null,
    ) : Dependency(
        id = group
    )

    data class Plugin(
        override val id: String,
        val version: String? = null
    ) : Dependency(
        id = id
    )
}