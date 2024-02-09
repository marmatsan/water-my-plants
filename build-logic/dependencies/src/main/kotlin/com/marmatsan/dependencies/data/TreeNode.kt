package com.marmatsan.dependencies.data

class TreeNode<T : NodeData>(
    private val data: T
) {
    private val children: MutableList<TreeNode<T>> by lazy {
        mutableListOf()
    }

    private fun hasChildren(): Boolean = children.size >= 1
    private fun isLeaf(): Boolean = !hasChildren()

    fun add(child: TreeNode<T>) = children.add(child)

    fun getLibraries(
        path: MutableList<String> = mutableListOf(),
        result: MutableList<Dependency.Library> = mutableListOf()
    ): List<Dependency.Library> {
        val nodeData = data as NodeData.Library
        path.add("${nodeData.id}.")

        if (isLeaf()) {
            val joinedPathWithoutLastDot = path.joinToString(separator = "") { it }.removeSuffix(suffix = ".")
            result.add(
                Dependency.Library(
                    group = joinedPathWithoutLastDot,
                    artifactsGroups = nodeData.artifactsGroups?.map { artifactsGroup ->
                        Dependency.ArtifactsGroup(
                            name = artifactsGroup.name,
                            artifacts = artifactsGroup.artifacts,
                            version = artifactsGroup.version
                        )
                    }
                )
            )
        } else {
            children.forEach { child ->
                child.getLibraries(
                    path = path,
                    result = result
                )
            }
        }
        path.removeLast()

        return result
    }

    fun getPlugins(
        path: MutableList<String> = mutableListOf(),
        result: MutableList<Dependency.Plugin> = mutableListOf()
    ): List<Dependency.Plugin> {
        val nodeData = data as NodeData.Plugin
        path.add("${nodeData.id}.")

        if (isLeaf()) {
            val joinedPathWithoutLastDot = path.joinToString(separator = "") { it }.removeSuffix(suffix = ".")
            result.add(
                Dependency.Plugin(
                    id = joinedPathWithoutLastDot,
                    version = nodeData.version
                )
            )
        } else {
            children.forEach { child ->
                child.getPlugins(
                    path = path,
                    result = result
                )
            }
        }
        path.removeLast()

        return result
    }
}