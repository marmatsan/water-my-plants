package com.marmatsan.verificationPlatform.domain.service

import com.marmatsan.verificationPlatform.domain.model.ModuleImpact
import com.marmatsan.verificationPlatform.domain.model.RepositoryModule
import com.marmatsan.verificationPlatform.domain.model.RepositoryModuleGraph

/** Resolves changed Gradle modules and their transitive reverse dependents. */
class ModuleImpactAnalyzer {
    /**
     * Calculates the modules that must be verified for [changedFiles].
     *
     * Invalid, empty, duplicate, or unresolved graph state produces an invalid
     * [ModuleImpact] with a fail-closed reason instead of a partial result.
     *
     * @param changedFiles normalized or platform-native repository paths.
     * @param graph provider-neutral Gradle project graph.
     * @return deterministic changed and affected module identifiers.
     */
    fun analyze(
        changedFiles: List<String>,
        graph: RepositoryModuleGraph,
    ): ModuleImpact {
        validate(
            graph = graph,
        )?.let { reason ->
            return ModuleImpact(
                changedModules = emptyList(),
                affectedModules = emptyList(),
                fallbackReason = reason,
            )
        }

        val changedModules =
            changedFiles
                .mapNotNull { path ->
                    moduleFor(
                        path = path,
                        graph = graph,
                    )
                }.map(
                    transform = RepositoryModule::id,
                ).distinct()
                .sorted()
        val reverseDependencies =
            graph.dependencies
                .groupBy(
                    keySelector = { dependency -> dependency.dependencyModule },
                    valueTransform = { dependency -> dependency.dependentModule },
                )
        val affectedModules =
            changedModules
                .flatMap { module ->
                    reverseClosure(
                        module = module,
                        reverseDependencies = reverseDependencies,
                    )
                }.distinct()
                .sorted()

        return ModuleImpact(
            changedModules = changedModules,
            affectedModules = affectedModules,
            fallbackReason = null,
        )
    }

    /**
     * Finds the most specific module directory that owns [path].
     *
     * @return the owning module, or `null` when the path is outside every
     * module in [graph].
     */
    fun moduleFor(
        path: String,
        graph: RepositoryModuleGraph,
    ): RepositoryModule? {
        val normalizedPath =
            normalize(
                path = path,
            )
        return graph.modules
            .filter { module ->
                val directory =
                    normalize(
                        path = module.directory,
                    ).trimEnd('/')
                normalizedPath == directory ||
                    normalizedPath.startsWith(
                        prefix = "$directory/",
                    )
            }.maxByOrNull { module ->
                normalize(
                    path = module.directory,
                ).length
            }
    }

    private fun validate(
        graph: RepositoryModuleGraph,
    ): String? {
        if (graph.modules.isEmpty()) {
            return "The Gradle module graph is empty; verification fails closed."
        }

        val invalidModule =
            graph.modules.firstOrNull { module ->
                !MODULE_ID.matches(module.id) ||
                    module.directory.isBlank() ||
                    normalize(
                        path = module.directory,
                    ).startsWith(
                        prefix = "../",
                    ) ||
                    normalize(
                        path = module.directory,
                    ).contains("/../")
            }
        if (invalidModule != null) {
            return "The Gradle module graph contains an invalid module: ${invalidModule.id}."
        }

        val duplicateId =
            graph.modules
                .groupingBy(RepositoryModule::id)
                .eachCount()
                .entries
                .firstOrNull { (_, count) -> count > 1 }
        if (duplicateId != null) {
            return "The Gradle module graph contains duplicate module id ${duplicateId.key}."
        }

        val duplicateDirectory =
            graph.modules
                .groupingBy { module ->
                    normalize(
                        path = module.directory,
                    ).trimEnd('/')
                }.eachCount()
                .entries
                .firstOrNull { (_, count) -> count > 1 }
        if (duplicateDirectory != null) {
            return "The Gradle module graph contains duplicate directory ${duplicateDirectory.key}."
        }

        val moduleIds =
            graph.modules
                .map(
                    transform = RepositoryModule::id,
                ).toSet()
        val unresolvedDependency =
            graph.dependencies.firstOrNull { dependency ->
                dependency.dependentModule !in moduleIds || dependency.dependencyModule !in moduleIds
            }
        if (unresolvedDependency != null) {
            return "The Gradle module graph contains an unresolved dependency: " +
                "${unresolvedDependency.dependentModule} -> ${unresolvedDependency.dependencyModule}."
        }

        return null
    }

    private fun reverseClosure(
        module: String,
        reverseDependencies: Map<String, List<String>>,
    ): Set<String> {
        val visited = linkedSetOf<String>()
        val pending = ArrayDeque<String>()
        pending.add(
            element = module,
        )

        while (pending.isNotEmpty()) {
            val current = pending.removeFirst()
            if (!visited.add(
                    element = current,
                )
            ) {
                continue
            }
            reverseDependencies[current].orEmpty().sorted().forEach(pending::addLast)
        }

        return visited
    }

    private fun normalize(
        path: String,
    ): String =
        path.trim().replace(
            '\\',
            '/',
        )

    private companion object {
        val MODULE_ID = Regex("^(:[A-Za-z0-9_.-]+)+$")
    }
}
