package com.marmatsan.dependencies.tree.dsl.library

import com.marmatsan.dependencies.tree.TreeBuilder
import com.marmatsan.dependencies.tree.dsl.path.DependencyPath
import com.marmatsan.dependencies.tree.dsl.path.resolvePath
import com.marmatsan.dependencies.tree.model.Artifact
import com.marmatsan.dependencies.tree.model.ArtifactsBundle
import com.marmatsan.dependencies.tree.model.DependencyNode
import com.marmatsan.dependencies.tree.model.LibraryEntry
import com.marmatsan.dependencies.tree.node.Node

/**
 * Builds library dependency nodes, entries, and relative group paths below [root].
 *
 * @param root Node whose subtree is configured by this scope.
 */
class LibraryScope(
    root: Node<DependencyNode.Library>
) : TreeBuilder<DependencyNode.Library>(root) {
    private var entries: MutableList<LibraryEntry>? = root.value.entries?.toMutableList()

    /** Returns the immutable entries configured directly on this scope's node. */
    internal fun configuredEntries(): List<LibraryEntry>? = entries?.toList()

    /**
     * Adds a single artifact entry to the current library.
     *
     * The enclosing root and [library] path provide the Maven group, producing the coordinate
     * `<full-group>:[artifact]`. Catalog registration derives the alias from that full group and
     * artifact, collapsing a repeated group suffix and replacing remaining artifact hyphens with
     * dots. For example, `io.ktor:ktor-client-core` becomes `libs.io.ktor.client.core` in the
     * default catalog.
     *
     * If [version] is `null`, the entry is registered with `withoutVersion()` and must receive its
     * version from a BOM or another dependency constraint.
     *
     * @param artifact The name of the artifact (e.g., `"activity-compose"`).
     * @param version The optional version of the artifact. If `null`, the artifact will be registered without a version.
     */
    fun artifact(
        artifact: String,
        version: String? = null
    ) {
        val newEntry =
            LibraryEntry.Single(
                artifact =
                    Artifact(
                        artifact,
                        version
                    )
            )
        entries =
            (entries ?: mutableListOf()).apply {
                add(
                    element = newEntry
                )
            }
    }

    /**
     * Adds a bundle of artifacts that share the same version and are grouped under a common alias.
     *
     * This is useful for defining a logical group of related dependencies that can be referenced
     * together via the specified [alias], used when defining library bundles in a Gradle
     * [Version Catalog](https://docs.gradle.org/current/userguide/version_catalogs.html).
     *
     * Each artifact is also registered as an individual library alias derived by [artifact]. If
     * [version] is `null`, every entry is versionless and must be managed by a BOM or another
     * dependency constraint.
     *
     * @param artifacts A vararg list of artifact names to include in the bundle (e.g., `"ui"`, `"ui-tooling"`).
     * @param alias The unique alias that identifies the bundle. It must end in `Bundle` and is
     * used to reference all artifacts together via `libs.bundles.<alias>`.
     * @param version The shared version for all artifacts in the bundle. If `null`, the version will not be declared.
     */
    fun artifactsBundle(
        vararg artifacts: String,
        alias: String,
        version: String? = null
    ) {
        val newEntry =
            LibraryEntry.Bundle(
                artifactsBundle =
                    ArtifactsBundle(
                        alias = alias,
                        artifacts =
                            artifacts.map {
                                Artifact(
                                    it,
                                    version
                                )
                            },
                        version = version
                    )
            )
        entries =
            (entries ?: mutableListOf()).apply {
                add(
                    element = newEntry
                )
            }
    }

    /**
     * Defines and registers a relative library path within the current [LibraryScope] tree.
     *
     * Dots in [group] delimit path segments, so `library("figma.code")` is equivalent to nesting
     * `library("figma") { library("code") { ... } }`. Existing prefixes are reused and [content]
     * is applied to the terminal node. A path without dots retains the ordinary single-node form.
     * Nodes without entries remain namespace nodes and are not registered as library aliases.
     *
     *
     * **Example**
     * ```
     * library("compose") {
     *     artifact("compose-bom", version = "2025.06.01")
     *     library("ui") {
     *         artifactsBundle(
     *             "ui", "ui-tooling", "ui-preview",
     *             alias = "composeUiBundle", version = "1.6.0"
     *         )
     *     }
     * }
     * ```
     *
     * @param group Relative group path (e.g., `"compose"` or `"figma.code"`).
     * @param content A DSL block that configures the entries (artifacts or nested groups) for this library group.
     * @throws IllegalArgumentException if [group] is not a valid dependency path.
     */
    fun library(
        group: String,
        content: (LibraryScope.() -> Unit)? = null
    ) {
        val resolvedNode =
            currentParent.resolvePath(
                path = DependencyPath.parse(group),
                segment = DependencyNode.Library::libraryGroup,
                createValue = { pathSegment ->
                    DependencyNode.Library(
                        libraryGroup = pathSegment
                    )
                }
            )

        val childScope =
            LibraryScope(
                root = resolvedNode.terminalNode()
            )
        content?.invoke(
            childScope
        )

        resolvedNode.replaceValue(
            value =
                resolvedNode.value().copy(
                    entries = childScope.entries?.toList()
                )
        )
    }
}
