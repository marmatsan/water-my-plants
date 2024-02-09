package com.marmatsan.dependencies.data

sealed class NodeData(
    open val id: String
) {

    data class ArtifactsGroup(
        val name : String,
        val artifacts: List<String>,
        val version: String
    )

    data class Library(
        val group: String,
        val artifactsGroups: List<ArtifactsGroup>? = null,
    ) : NodeData(
        id = group
    )

    data class Plugin(
        override val id: String,
        val version : String? = null
    ) : NodeData(
        id = id
    )
}