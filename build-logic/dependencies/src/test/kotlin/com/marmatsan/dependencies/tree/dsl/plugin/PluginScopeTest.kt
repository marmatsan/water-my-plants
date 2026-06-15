package com.marmatsan.dependencies.tree.dsl.plugin

import com.marmatsan.dependencies.tree.model.DependencyNode
import com.marmatsan.dependencies.tree.node.Node
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class PluginScopeTest : FunSpec({

    test("plugin adds a child plugin node with version") {
        val root = Node(DependencyNode.Plugin("root"))
        val scope = PluginScope(root)

        scope.plugin("com.android.application", version = "9.2.1")

        root.children shouldBe mutableListOf(
                Node(
                    DependencyNode.Plugin(
                        pluginId = "com.android.application",
                        version = "9.2.1"
                    )
                )
            )
    }

    test("plugin adds a child plugin node without version") {
        val root = Node(DependencyNode.Plugin("root"))
        val scope = PluginScope(root)

        scope.plugin("com.marmatsan.android")

        root.children shouldBe mutableListOf(
                Node(
                    DependencyNode.Plugin(
                        pluginId = "com.marmatsan.android"
                    )
                )
            )
    }

    test("plugin supports nested plugin groups") {
        val root = Node(DependencyNode.Plugin("root"))
        val scope = PluginScope(root)

        scope.plugin("org.jetbrains.kotlin") {
            plugin("android", version = "2.3.21")
            plugin("plugin") {
                plugin("compose", version = "2.3.21")
            }
        }

        root.children shouldBe mutableListOf(
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
    }

    test("plugin restores parent after nested content and keeps sibling order") {
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

        root.children shouldBe mutableListOf(
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
    }
})
