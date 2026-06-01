package com.marmatsan.dependencies.tree.plugin

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.marmatsan.dependencies.tree.model.DependencyNode
import com.marmatsan.dependencies.tree.node.Node
import org.junit.jupiter.api.Test

internal class PluginScopeTest {

    @Test
    fun `plugin adds a child plugin node with version`() {
        val root = Node(DependencyNode.Plugin("root"))
        val scope = PluginScope(root)

        scope.plugin("com.android.application", version = "9.2.1")

        assertThat(root.children).isEqualTo(
            mutableListOf(
                Node(
                    DependencyNode.Plugin(
                        pluginId = "com.android.application",
                        version = "9.2.1"
                    )
                )
            )
        )
    }

    @Test
    fun `plugin adds a child plugin node without version`() {
        val root = Node(DependencyNode.Plugin("root"))
        val scope = PluginScope(root)

        scope.plugin("com.marmatsan.android")

        assertThat(root.children).isEqualTo(
            mutableListOf(
                Node(
                    DependencyNode.Plugin(
                        pluginId = "com.marmatsan.android"
                    )
                )
            )
        )
    }

    @Test
    fun `plugin supports nested plugin groups`() {
        val root = Node(DependencyNode.Plugin("root"))
        val scope = PluginScope(root)

        scope.plugin("org.jetbrains.kotlin") {
            plugin("android", version = "2.3.21")
            plugin("plugin") {
                plugin("compose", version = "2.3.21")
            }
        }

        assertThat(root.children).isEqualTo(
            mutableListOf(
                Node(
                    value = DependencyNode.Plugin(
                        pluginId = "org.jetbrains.kotlin"
                    ),
                    children = mutableListOf(
                        Node(
                            DependencyNode.Plugin(
                                pluginId = "android",
                                version = "2.3.21"
                            )
                        ),
                        Node(
                            value = DependencyNode.Plugin(
                                pluginId = "plugin"
                            ),
                            children = mutableListOf(
                                Node(
                                    DependencyNode.Plugin(
                                        pluginId = "compose",
                                        version = "2.3.21"
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `plugin restores parent after nested content and keeps sibling order`() {
        val root = Node(DependencyNode.Plugin("root"))
        val scope = PluginScope(root)

        scope.plugin("com.android") {
            plugin("application", version = "9.2.1")
        }
        scope.plugin("com.google") {
            plugin("devtools") {
                plugin("ksp", version = "2.3.9")
            }
        }

        assertThat(root.children).isEqualTo(
            mutableListOf(
                Node(
                    value = DependencyNode.Plugin(
                        pluginId = "com.android"
                    ),
                    children = mutableListOf(
                        Node(
                            DependencyNode.Plugin(
                                pluginId = "application",
                                version = "9.2.1"
                            )
                        )
                    )
                ),
                Node(
                    value = DependencyNode.Plugin(
                        pluginId = "com.google"
                    ),
                    children = mutableListOf(
                        Node(
                            value = DependencyNode.Plugin(
                                pluginId = "devtools"
                            ),
                            children = mutableListOf(
                                Node(
                                    DependencyNode.Plugin(
                                        pluginId = "ksp",
                                        version = "2.3.9"
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    }
}