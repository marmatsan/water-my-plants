package com.marmatsan.dependencies.tree.mapper

import com.marmatsan.dependencies.tree.model.Dependency
import com.marmatsan.dependencies.tree.model.DependencyNode

/**
 * Maps a [DependencyNode.Library] node payload into a [Dependency.Library].
 *
 * Note: The [libraryGroup] parameter is used as the output group identifier, even though
 * [DependencyNode.Library] already contains a `libraryGroup` property. Ensure they are consistent
 * to avoid generating dependencies under the wrong group.
 *
 * @param libraryGroup The group identifier to set on the resulting [Dependency.Library].
 * @return A [Dependency.Library] equivalent to this [DependencyNode.Library].
 */
fun DependencyNode.Library.toDependencyLibrary(
    libraryGroup: String
): Dependency.Library = Dependency.Library(
    libraryGroup = libraryGroup,
    entries = entries
)

/**
 * Maps a [DependencyNode.Plugin] node payload into a [Dependency.Plugin].
 *
 * The [pluginId] parameter is used as the output id, even though [DependencyNode.Plugin] already contains a `pluginId`
 * property. The source node must have a version because [Dependency.Plugin] represents a final registrable plugin.
 *
 * @param pluginId The plugin id to set on the resulting [Dependency.Plugin].
 * @return A [Dependency.Plugin] equivalent to this [DependencyNode.Plugin].
 * @throws IllegalArgumentException When this node does not declare a plugin version.
 */
fun DependencyNode.Plugin.toDependencyPlugin(
    pluginId: String
) = Dependency.Plugin(
    pluginId = pluginId,
    version = requireNotNull(version) {
        "Plugin node '$pluginId' must declare a version to be mapped to Dependency.Plugin"
    }
)
