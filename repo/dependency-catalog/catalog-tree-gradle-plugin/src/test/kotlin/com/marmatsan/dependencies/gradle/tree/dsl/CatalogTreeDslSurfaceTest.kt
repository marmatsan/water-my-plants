package com.marmatsan.dependencies.gradle.tree.dsl

import com.marmatsan.unitTest.dsl.given
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.lang.reflect.Modifier

internal class CatalogTreeDslSurfaceTest :
    FunSpec(
        {
            test("top-level catalog scopes expose only the root authoring operation") {
                given {
                    listOf(
                        LibraryCatalogTreesScope::class.java,
                        PluginCatalogTreesScope::class.java
                    )
                }.whenever { scopeTypes ->
                    scopeTypes.associate { scopeType ->
                        scopeType.simpleName to scopeType.publicDslOperationNames()
                    }
                }.then { operationsByScope ->
                    operationsByScope shouldBe
                        mapOf(
                            "LibraryCatalogTreesScope" to setOf("root"),
                            "PluginCatalogTreesScope" to setOf("root")
                        )
                }
            }
        }
    )

/** Returns the non-synthetic public operations declared directly by this DSL scope. */
private fun Class<*>.publicDslOperationNames(): Set<String> =
    declaredMethods
        .asSequence()
        .filter { method -> Modifier.isPublic(method.modifiers) }
        .filterNot { method -> method.isSynthetic || '$' in method.name }
        .map { method -> method.name }
        .toSet()
