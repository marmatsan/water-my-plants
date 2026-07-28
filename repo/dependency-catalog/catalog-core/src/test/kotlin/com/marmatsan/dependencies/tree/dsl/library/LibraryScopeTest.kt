package com.marmatsan.dependencies.tree.dsl.library

import com.marmatsan.dependencies.tree.model.Artifact
import com.marmatsan.dependencies.tree.model.ArtifactsBundle
import com.marmatsan.dependencies.tree.model.DependencyNode
import com.marmatsan.dependencies.tree.model.LibraryEntry
import com.marmatsan.dependencies.tree.node.Node
import com.marmatsan.unitTest.dsl.given
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class LibraryScopeTest :
    FunSpec(
        {
            test("libraryTree preserves artifacts declared directly on its root") {
                given {
                    libraryTree(
                        rootGroup = "tools",
                    ) {
                        artifact(
                            artifact = "core",
                            version = "1.2.3",
                        )
                    }
                }.whenever { root ->
                    root
                }.then { root ->
                    root.value shouldBe
                        DependencyNode.Library(
                            libraryGroup = "tools",
                            entries =
                                listOf(
                                    LibraryEntry.Single(
                                        artifact =
                                            Artifact(
                                                artifact = "core",
                                                version = "1.2.3",
                                            ),
                                    ),
                                ),
                        )
                }
            }

            test("libraryTree rejects a compact path as its root") {
                given {
                    "org.jetbrains"
                }.whenever { compactRoot ->
                    shouldThrow<IllegalArgumentException> {
                        libraryTree(
                            rootGroup = compactRoot,
                        ) {}
                    }
                }.then { failure ->
                    failure.message shouldBe
                        "Library group 'org.jetbrains' must be one non-blank path segment without dots or whitespace"
                }
            }

            test("artifact adds a single entry with version to the created library") {
                given(::libraryScopeFixture)
                    .whenever { fixture ->
                        fixture.scope.library("activity") {
                            artifact(
                                artifact = "activity-compose",
                                version = "1.9.1",
                            )
                        }
                        fixture.root
                    }.then { root ->
                        root.children shouldBe
                            mutableListOf(
                                Node(
                                    DependencyNode.Library(
                                        libraryGroup = "activity",
                                        entries =
                                            listOf(
                                                LibraryEntry.Single(
                                                    artifact =
                                                        Artifact(
                                                            "activity-compose",
                                                            "1.9.1",
                                                        ),
                                                ),
                                            ),
                                    ),
                                ),
                            )
                    }
            }

            test("artifact adds multiple entries and keeps versionless artifacts and declaration order") {
                given(::libraryScopeFixture)
                    .whenever { fixture ->
                        fixture.scope.library("activity") {
                            artifact(
                                artifact = "activity-compose",
                                version = "1.9.1",
                            )
                            artifact(
                                artifact = "activity-ktx",
                            )
                        }
                        fixture.root
                    }.then { root ->
                        root.children shouldBe
                            mutableListOf(
                                Node(
                                    DependencyNode.Library(
                                        libraryGroup = "activity",
                                        entries =
                                            listOf(
                                                LibraryEntry.Single(
                                                    artifact =
                                                        Artifact(
                                                            "activity-compose",
                                                            "1.9.1",
                                                        ),
                                                ),
                                                LibraryEntry.Single(
                                                    artifact = Artifact("activity-ktx"),
                                                ),
                                            ),
                                    ),
                                ),
                            )
                    }
            }

            test("artifactsBundle adds a bundle entry with alias and versionless artifacts") {
                given(::libraryScopeFixture)
                    .whenever { fixture ->
                        fixture.scope.library("compose") {
                            artifactsBundle(
                                "ui",
                                "ui-graphics",
                                "ui-tooling",
                                alias = "composeBundle",
                            )
                        }
                        fixture.root
                    }.then { root ->
                        root.children shouldBe
                            mutableListOf(
                                Node(
                                    DependencyNode.Library(
                                        libraryGroup = "compose",
                                        entries =
                                            listOf(
                                                LibraryEntry.Bundle(
                                                    artifactsBundle =
                                                        ArtifactsBundle(
                                                            alias = "composeBundle",
                                                            artifacts =
                                                                listOf(
                                                                    Artifact("ui"),
                                                                    Artifact("ui-graphics"),
                                                                    Artifact("ui-tooling"),
                                                                ),
                                                        ),
                                                ),
                                            ),
                                    ),
                                ),
                            )
                    }
            }

            test("artifactsBundle propagates version to bundle and artifacts") {
                given(::libraryScopeFixture)
                    .whenever { fixture ->
                        fixture.scope.library("compose") {
                            artifactsBundle(
                                "ui",
                                "ui-graphics",
                                alias = "composeBundle",
                                version = "1.7.0",
                            )
                        }
                        fixture.root
                    }.then { root ->
                        root.children shouldBe
                            mutableListOf(
                                Node(
                                    DependencyNode.Library(
                                        libraryGroup = "compose",
                                        entries =
                                            listOf(
                                                LibraryEntry.Bundle(
                                                    artifactsBundle =
                                                        ArtifactsBundle(
                                                            alias = "composeBundle",
                                                            artifacts =
                                                                listOf(
                                                                    Artifact(
                                                                        "ui",
                                                                        "1.7.0",
                                                                    ),
                                                                    Artifact(
                                                                        "ui-graphics",
                                                                        "1.7.0",
                                                                    ),
                                                                ),
                                                            version = "1.7.0",
                                                        ),
                                                ),
                                            ),
                                    ),
                                ),
                            )
                    }
            }

            test("artifactsBundle can be declared with artifact and keeps entry order") {
                given(::libraryScopeFixture)
                    .whenever { fixture ->
                        fixture.scope.library("compose") {
                            artifact(
                                artifact = "compose-bom",
                                version = "2025.06.01",
                            )
                            artifactsBundle(
                                "ui",
                                "ui-graphics",
                                alias = "composeBundle",
                            )
                        }
                        fixture.root
                    }.then { root ->
                        root.children shouldBe
                            mutableListOf(
                                Node(
                                    DependencyNode.Library(
                                        libraryGroup = "compose",
                                        entries =
                                            listOf(
                                                LibraryEntry.Single(
                                                    artifact =
                                                        Artifact(
                                                            "compose-bom",
                                                            "2025.06.01",
                                                        ),
                                                ),
                                                LibraryEntry.Bundle(
                                                    artifactsBundle =
                                                        ArtifactsBundle(
                                                            alias = "composeBundle",
                                                            artifacts =
                                                                listOf(
                                                                    Artifact("ui"),
                                                                    Artifact("ui-graphics"),
                                                                ),
                                                        ),
                                                ),
                                            ),
                                    ),
                                ),
                            )
                    }
            }

            test("library adds a child library node without entries when content is null") {
                given(::libraryScopeFixture)
                    .whenever { fixture ->
                        fixture.scope.library("compose")
                        fixture.root
                    }.then { root ->
                        root.children shouldBe
                            mutableListOf(
                                Node(
                                    DependencyNode.Library(
                                        libraryGroup = "compose",
                                    ),
                                ),
                            )
                    }
            }

            test("library supports nested groups and preserves parent entries and children") {
                given(::libraryScopeFixture)
                    .whenever { fixture ->
                        fixture.scope.library("compose") {
                            artifact(
                                artifact = "compose-bom",
                                version = "2025.06.01",
                            )
                            library("ui") {
                                artifact(
                                    artifact = "ui",
                                )
                            }
                        }
                        fixture.root
                    }.then { root ->
                        root.children shouldBe
                            mutableListOf(
                                Node(
                                    value =
                                        DependencyNode.Library(
                                            libraryGroup = "compose",
                                            entries =
                                                listOf(
                                                    LibraryEntry.Single(
                                                        artifact =
                                                            Artifact(
                                                                "compose-bom",
                                                                "2025.06.01",
                                                            ),
                                                    ),
                                                ),
                                        ),
                                    children =
                                        mutableListOf(
                                            Node(
                                                DependencyNode.Library(
                                                    libraryGroup = "ui",
                                                    entries =
                                                        listOf(
                                                            LibraryEntry.Single(
                                                                artifact = Artifact("ui"),
                                                            ),
                                                        ),
                                                ),
                                            ),
                                        ),
                                ),
                            )
                    }
            }

            test("library keeps entries isolated between siblings and preserves sibling order") {
                given(::libraryScopeFixture)
                    .whenever { fixture ->
                        fixture.scope.library("activity") {
                            artifact(
                                artifact = "activity-compose",
                                version = "1.9.1",
                            )
                        }
                        fixture.scope.library("compose") {
                            artifact(
                                artifact = "compose-bom",
                                version = "2025.06.01",
                            )
                        }
                        fixture.root
                    }.then { root ->
                        root.children shouldBe
                            mutableListOf(
                                Node(
                                    DependencyNode.Library(
                                        libraryGroup = "activity",
                                        entries =
                                            listOf(
                                                LibraryEntry.Single(
                                                    artifact =
                                                        Artifact(
                                                            "activity-compose",
                                                            "1.9.1",
                                                        ),
                                                ),
                                            ),
                                    ),
                                ),
                                Node(
                                    DependencyNode.Library(
                                        libraryGroup = "compose",
                                        entries =
                                            listOf(
                                                LibraryEntry.Single(
                                                    artifact =
                                                        Artifact(
                                                            "compose-bom",
                                                            "2025.06.01",
                                                        ),
                                                ),
                                            ),
                                    ),
                                ),
                            )
                    }
            }

            test("library expands a compact path and applies artifacts to its terminal node") {
                given(::libraryScopeFixture)
                    .whenever { fixture ->
                        fixture.scope.library("figma.code.connect") {
                            artifact(
                                artifact = "code-connect-lib",
                                version = "1.2.3",
                            )
                        }
                        fixture.root
                    }.then { root ->
                        root.children shouldBe
                            mutableListOf(
                                libraryNode(
                                    group = "figma",
                                    children =
                                        mutableListOf(
                                            libraryNode(
                                                group = "code",
                                                children =
                                                    mutableListOf(
                                                        libraryNode(
                                                            group = "connect",
                                                            entries =
                                                                listOf(
                                                                    LibraryEntry.Single(
                                                                        artifact =
                                                                            Artifact(
                                                                                artifact = "code-connect-lib",
                                                                                version = "1.2.3",
                                                                            ),
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

            test("library reuses compact path prefixes and preserves child declaration order") {
                given(::libraryScopeFixture)
                    .whenever { fixture ->
                        fixture.scope.library("compose.ui") {
                            artifact(
                                artifact = "ui",
                            )
                        }
                        fixture.scope.library("compose.material3") {
                            artifact(
                                artifact = "material3",
                            )
                        }
                        fixture.root
                    }.then { root ->
                        root.children shouldBe
                            mutableListOf(
                                libraryNode(
                                    group = "compose",
                                    children =
                                        mutableListOf(
                                            libraryNode(
                                                group = "ui",
                                                entries =
                                                    listOf(
                                                        LibraryEntry.Single(
                                                            artifact = Artifact("ui"),
                                                        ),
                                                    ),
                                            ),
                                            libraryNode(
                                                group = "material3",
                                                entries =
                                                    listOf(
                                                        LibraryEntry.Single(
                                                            artifact = Artifact("material3"),
                                                        ),
                                                    ),
                                            ),
                                        ),
                                ),
                            )
                    }
            }

            test("library appends entries when the same compact terminal path is configured again") {
                given(::libraryScopeFixture)
                    .whenever { fixture ->
                        fixture.scope.library("compose.ui") {
                            artifact(
                                artifact = "ui",
                            )
                        }
                        fixture.scope.library("compose.ui") {
                            artifact(
                                artifact = "ui-tooling",
                            )
                        }
                        fixture.root
                    }.then { root ->
                        root.children shouldBe
                            mutableListOf(
                                libraryNode(
                                    group = "compose",
                                    children =
                                        mutableListOf(
                                            libraryNode(
                                                group = "ui",
                                                entries =
                                                    listOf(
                                                        LibraryEntry.Single(
                                                            artifact = Artifact("ui"),
                                                        ),
                                                        LibraryEntry.Single(
                                                            artifact = Artifact("ui-tooling"),
                                                        ),
                                                    ),
                                            ),
                                        ),
                                ),
                            )
                    }
            }

            test("library rejects a path with whitespace") {
                given(::libraryScopeFixture)
                    .whenever { fixture ->
                        shouldThrow<IllegalArgumentException> {
                            fixture.scope.library("figma. code")
                        }
                    }.then { failure ->
                        failure.message shouldBe
                            "Dependency path 'figma. code' must contain non-blank segments without whitespace"
                    }
            }
        },
    )

private fun libraryNode(
    group: String,
    entries: List<LibraryEntry>? = null,
    children: MutableList<Node<DependencyNode.Library>> = mutableListOf(),
): Node<DependencyNode.Library> =
    Node(
        value =
            DependencyNode.Library(
                libraryGroup = group,
                entries = entries,
            ),
        children = children,
    )

private fun libraryScopeFixture(): LibraryScopeFixture {
    val root =
        Node(
            DependencyNode.Library(
                libraryGroup = "androidx",
            ),
        )
    return LibraryScopeFixture(
        root = root,
        scope =
            LibraryScope(
                root = root,
            ),
    )
}

private data class LibraryScopeFixture(
    val root: Node<DependencyNode.Library>,
    val scope: LibraryScope,
)
