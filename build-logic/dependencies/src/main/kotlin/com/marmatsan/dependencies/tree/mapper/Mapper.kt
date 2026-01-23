package com.marmatsan.dependencies.tree.mapper

import com.marmatsan.dependencies.tree.model.Dependency
import com.marmatsan.dependencies.tree.model.NodeData

/**
 * Maps a [NodeData.Library] node payload into a [Dependency.Library].
 *
 * This function performs a structural conversion of entries:
 * - [NodeData.Library.Entry.Single] -> [Dependency.Library.Entry.Single]
 * - [NodeData.Library.Entry.Bundle] -> [Dependency.Library.Entry.Bundle]
 *
 * Artifact names and versions are copied as-is. If [NodeData.Library.entries] is `null`,
 * the resulting [Dependency.Library.entries] will also be `null`.
 *
 * Note: The [libraryGroup] parameter is used as the output group identifier, even though
 * [NodeData.Library] already contains a `libraryGroup` property. Ensure they are consistent
 * to avoid generating dependencies under the wrong group.
 *
 * @param libraryGroup The group identifier to set on the resulting [Dependency.Library].
 * @return A [Dependency.Library] equivalent to this [NodeData.Library].
 */
fun NodeData.Library.toDependencyLibrary(
    libraryGroup: String
): Dependency.Library {
    val entries = this.entries?.map { entry ->
        when (entry) {
            is NodeData.Library.Entry.Single ->
                Dependency.Library.Entry.Single(
                    Dependency.Library.Artifact(
                        artifact = entry.artifact.artifact,
                        version = entry.artifact.version
                    )
                )

            is NodeData.Library.Entry.Bundle ->
                Dependency.Library.Entry.Bundle(
                    Dependency.Library.ArtifactsBundle(
                        alias = entry.artifactsBundle.alias,
                        artifacts = entry.artifactsBundle.artifacts.map { artifact ->
                            Dependency.Library.Artifact(
                                artifact = artifact.artifact,
                                version = artifact.version
                            )
                        },
                        version = entry.artifactsBundle.version
                    )
                )
        }
    }

    return Dependency.Library(
        libraryGroup = libraryGroup,
        entries = entries
    )
}

/**
 * Maps a [NodeData.Plugin] node payload into a [Dependency.Plugin].
 *
 * The plugin version is copied as-is. The [pluginId] parameter is used as the output id,
 * even though [NodeData.Plugin] already contains a `pluginId` property.
 *
 * @param pluginId The plugin id to set on the resulting [Dependency.Plugin].
 * @return A [Dependency.Plugin] equivalent to this [NodeData.Plugin].
 */
fun NodeData.Plugin.toDependencyPlugin(
    pluginId: String
) = Dependency.Plugin(
    pluginId = pluginId,
    version = version
)