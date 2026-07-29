package com.marmatsan.dependencies.tree.node

import com.marmatsan.dependencies.tree.mapper.toDependencyLibrary
import com.marmatsan.dependencies.tree.mapper.toDependencyPlugin
import com.marmatsan.dependencies.tree.model.Dependency
import com.marmatsan.dependencies.tree.model.DependencyNode

/**
 * Flattens this library tree into registrable dependencies using complete dotted group paths.
 *
 * Namespace nodes without entries are omitted while their path values remain part of descendant
 * groups.
 *
 * @return Library dependencies in depth-first pre-order.
 */
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

/**
 * Flattens this plugin tree into registrable dependencies using complete dotted plugin ids.
 *
 * Namespace nodes without versions are omitted while their id values remain part of descendant
 * plugin ids.
 *
 * @return Versioned plugin dependencies in depth-first pre-order.
 */
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
