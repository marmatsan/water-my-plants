package com.marmatsan.dependencies.tree.node

import com.marmatsan.dependencies.tree.mapper.toDependencyLibrary
import com.marmatsan.dependencies.tree.mapper.toDependencyPlugin
import com.marmatsan.dependencies.tree.model.Dependency
import com.marmatsan.dependencies.tree.model.DependencyNode

fun Node<DependencyNode.Library>.toDependencyLibraries(): List<Dependency.Library> =
    depthFirstPreOrderTraverse(
        pathSegment = DependencyNode.Library::libraryGroup,
        shouldIncludeNode = { libraryNode ->
            libraryNode.entries != null
        },
        mapNode = { libraryNode, libraryGroup ->
            libraryNode.toDependencyLibrary(
                libraryGroup = libraryGroup
            )
        }
    )

fun Node<DependencyNode.Plugin>.toDependencyPlugins(): List<Dependency.Plugin> =
    depthFirstPreOrderTraverse(
        pathSegment = DependencyNode.Plugin::pluginId,
        shouldIncludeNode = { pluginNode ->
            pluginNode.version != null
        },
        mapNode = { pluginNode, pluginId ->
            pluginNode.toDependencyPlugin(
                pluginId = pluginId
            )
        }
    )
