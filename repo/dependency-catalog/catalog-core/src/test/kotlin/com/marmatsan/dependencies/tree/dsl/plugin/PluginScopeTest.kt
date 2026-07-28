package com.marmatsan.dependencies.tree.dsl.plugin

import com.marmatsan.dependencies.tree.model.DependencyNode
import com.marmatsan.dependencies.tree.node.Node
import com.marmatsan.unitTest.dsl.given
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class PluginScopeTest :
    FunSpec(
        {
            test("plugin expands a compact path and assigns the version to its terminal node") {
                given(::pluginScopeFixture)
                    .whenever { fixture ->
                        fixture.scope.plugin(
                            id = "figma.code.connect",
                            version = "1.2.3",
                        )
                        fixture.root
                    }.then { root ->
                        root.children shouldBe
                            mutableListOf(
                                pluginNode(
                                    id = "figma",
                                    children =
                                        mutableListOf(
                                            pluginNode(
                                                id = "code",
                                                children =
                                                    mutableListOf(
                                                        pluginNode(
                                                            id = "connect",
                                                            version = "1.2.3",
                                                        ),
                                                    ),
                                            ),
                                        ),
                                ),
                            )
                    }
            }

            test("plugin executes nested declarations below the terminal compact path node") {
                given(::pluginScopeFixture)
                    .whenever { fixture ->
                        fixture.scope.plugin("figma.code") {
                            plugin(
                                id = "connect",
                                version = "1.2.3",
                            )
                        }
                        fixture.root
                    }.then { root ->
                        root.children shouldBe
                            mutableListOf(
                                pluginNode(
                                    id = "figma",
                                    children =
                                        mutableListOf(
                                            pluginNode(
                                                id = "code",
                                                children =
                                                    mutableListOf(
                                                        pluginNode(
                                                            id = "connect",
                                                            version = "1.2.3",
                                                        ),
                                                    ),
                                            ),
                                        ),
                                ),
                            )
                    }
            }

            test("plugin reuses compact path prefixes and preserves terminal sibling order") {
                given(::pluginScopeFixture)
                    .whenever { fixture ->
                        fixture.scope.plugin("jetbrains.kotlin") {
                            plugin(
                                id = "jvm",
                                version = "2.3.21",
                            )
                        }
                        fixture.scope.plugin("jetbrains.kotlin.plugin") {
                            plugin(
                                id = "compose",
                                version = "2.3.21",
                            )
                        }
                        fixture.root
                    }.then { root ->
                        root.children shouldBe
                            mutableListOf(
                                pluginNode(
                                    id = "jetbrains",
                                    children =
                                        mutableListOf(
                                            pluginNode(
                                                id = "kotlin",
                                                children =
                                                    mutableListOf(
                                                        pluginNode(
                                                            id = "jvm",
                                                            version = "2.3.21",
                                                        ),
                                                        pluginNode(
                                                            id = "plugin",
                                                            children =
                                                                mutableListOf(
                                                                    pluginNode(
                                                                        id = "compose",
                                                                        version = "2.3.21",
                                                                    ),
                                                                ),
                                                        ),
                                                    ),
                                            ),
                                        ),
                                ),
                            )
                    }
            }

            test("plugin rejects a conflicting version for an existing terminal path") {
                given(::pluginScopeFixture)
                    .whenever { fixture ->
                        fixture.scope.plugin(
                            id = "android.application",
                            version = "9.2.1",
                        )
                        shouldThrow<IllegalArgumentException> {
                            fixture.scope.plugin(
                                id = "android.application",
                                version = "9.3.0",
                            )
                        }
                    }.then { failure ->
                        failure.message shouldBe
                            "Plugin path 'android.application' already declares version '9.2.1' " +
                            "and cannot declare '9.3.0'"
                    }
            }

            test("plugin rejects a path with an empty segment") {
                given(::pluginScopeFixture)
                    .whenever { fixture ->
                        shouldThrow<IllegalArgumentException> {
                            fixture.scope.plugin("figma..code")
                        }
                    }.then { failure ->
                        failure.message shouldBe
                            "Dependency path 'figma..code' must contain non-blank segments without surrounding whitespace"
                    }
            }
        },
    )

private fun pluginScopeFixture(): PluginScopeFixture {
    val root =
        pluginNode(
            id = "com",
        )
    return PluginScopeFixture(
        root = root,
        scope =
            PluginScope(
                root = root,
            ),
    )
}

private fun pluginNode(
    id: String,
    version: String? = null,
    children: MutableList<Node<DependencyNode.Plugin>> = mutableListOf(),
): Node<DependencyNode.Plugin> =
    Node(
        value =
            DependencyNode.Plugin(
                pluginId = id,
                version = version,
            ),
        children = children,
    )

private data class PluginScopeFixture(
    val root: Node<DependencyNode.Plugin>,
    val scope: PluginScope,
)
