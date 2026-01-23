package com.marmatsan.dependencies.tree.tree

import com.marmatsan.dependencies.tree.mapper.toDependencyLibrary
import com.marmatsan.dependencies.tree.mapper.toDependencyPlugin
import com.marmatsan.dependencies.tree.model.Dependency
import com.marmatsan.dependencies.tree.model.NodeData

fun TreeNode<NodeData.Library>.getLibraries(): List<Dependency.Library> =
    depthFirstPreOrderTraverse(
        pathSegment = { libraryNode ->
            libraryNode.libraryGroup
        },
        nodeIsLeaf = { libraryNode ->
            libraryNode.entries != null
        },
        mapNode = { libraryNode, libraryGroup ->
            libraryNode.toDependencyLibrary(libraryGroup = libraryGroup)
        }
    )

fun TreeNode<NodeData.Plugin>.getPlugins(): List<Dependency.Plugin> =
    depthFirstPreOrderTraverse(
        pathSegment = { pluginNode ->
            pluginNode.pluginId
        },
        nodeIsLeaf = { pluginNode ->
            pluginNode.version != null
        },
        mapNode = { pluginNode, pluginId ->
            pluginNode.toDependencyPlugin(pluginId = pluginId)
        }
    )