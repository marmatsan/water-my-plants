package com.marmatsan.verificationPlatform.data.kotlin

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files

class RepositoryKotlinStyleTest :
    StringSpec(
        {
            "formats every declaration parameter and multiple call arguments vertically" {
                // GIVEN
                val sourceFile =
                    Files
                        .createTempFile(
                            "FunctionArgumentLayout",
                            ".kt",
                        ).toFile()
                sourceFile.writeText(
                    """
                    fun greet(first: String, second: String) = combine(first, second)

                    fun printSingle(value: String) = println(value)
                    """.trimIndent(),
                )
                val layout = repositoryKotlinStyle()

                // WHEN
                val formatted =
                    layout.format(
                        file = sourceFile,
                    )

                // THEN
                formatted shouldBe
                    canonicalKotlin(
                        source = """
                        fun greet(
                            first: String,
                            second: String,
                        ) = combine(
                            first,
                            second,
                        )

                        fun printSingle(
                            value: String,
                        ) = println(value)
                        """,
                    )
            }

            "formats a single named argument vertically but leaves a clear positional argument inline" {
                // GIVEN
                val sourceFile =
                    Files
                        .createTempFile(
                            "NamedFunctionArgumentLayout",
                            ".kt",
                        ).toFile()
                sourceFile.writeText(
                    """
                    fun render(label: String) = Unit

                    fun show(value: String) {
                        render(label = value)
                        println(value)
                    }
                    """.trimIndent(),
                )
                val layout = repositoryKotlinStyle()

                // WHEN
                val formatted =
                    layout.format(
                        file = sourceFile,
                    )

                // THEN
                formatted shouldBe
                    canonicalKotlin(
                        source = """
                        fun render(
                            label: String,
                        ) = Unit

                        fun show(
                            value: String,
                        ) {
                            render(
                                label = value,
                            )
                            println(value)
                        }
                        """,
                    )
                sourceFile.writeText(formatted)
                layout.inspect(sourceFile).shouldBeEmpty()
            }

            "names arguments for repository Kotlin functions and constructors" {
                // GIVEN
                val sourceFile =
                    Files
                        .createTempFile(
                            "NamedRepositoryCallLayout",
                            ".kt",
                        ).toFile()
                sourceFile.writeText(
                    """
                    data class Visit(
                        val node: String,
                        val path: List<String>
                    )

                    fun visit(node: String, path: List<String>) = Visit(node, path)

                    fun walk(child: String, currentPath: List<String>) {
                        visit(child, currentPath)
                    }
                    """.trimIndent(),
                )
                val layout =
                    repositoryKotlinStyle(
                        sourceFiles = listOf(sourceFile),
                    )

                // WHEN
                val violations = layout.inspect(sourceFile)
                val formatted =
                    layout.format(
                        file = sourceFile,
                    )

                // THEN
                violations.shouldNotBeEmpty()
                formatted shouldBe
                    canonicalKotlin(
                        source = """
                        data class Visit(
                            val node: String,
                            val path: List<String>,
                        )

                        fun visit(
                            node: String,
                            path: List<String>,
                        ) = Visit(
                            node = node,
                            path = path,
                        )

                        fun walk(
                            child: String,
                            currentPath: List<String>,
                        ) {
                            visit(
                                node = child,
                                path = currentPath,
                            )
                        }
                        """,
                    )
                sourceFile.writeText(formatted)
                layout.inspect(sourceFile).shouldBeEmpty()
            }

            "leaves function value invocations positional because Kotlin does not support names" {
                // GIVEN
                val sourceFile =
                    Files
                        .createTempFile(
                            "FunctionValueCallLayout",
                            ".kt",
                        ).toFile()
                sourceFile.writeText(
                    """
                    fun transform(
                        mapNode: (value: String, fullPath: String) -> String,
                        value: String,
                        fullPath: String
                    ) = mapNode(value, fullPath)
                    """.trimIndent(),
                )
                val layout =
                    repositoryKotlinStyle(
                        sourceFiles = listOf(sourceFile),
                    )

                // WHEN
                val formatted =
                    layout.format(
                        file = sourceFile,
                    )

                // THEN
                formatted shouldBe
                    canonicalKotlin(
                        source = """
                        fun transform(
                            mapNode: (value: String, fullPath: String) -> String,
                            value: String,
                            fullPath: String,
                        ) = mapNode(
                            value,
                            fullPath,
                        )
                        """,
                    )
            }

            "indents nested function arguments relative to their own call" {
                // GIVEN
                val sourceFile =
                    Files
                        .createTempFile(
                            "NestedFunctionArgumentLayout",
                            ".kt",
                        ).toFile()
                sourceFile.writeText(
                    """
                    fun collect(
                        results: MutableList<String>,
                        mapNode: (value: String) -> String,
                        value: String
                    ) {
                        results.add(
                            element = mapNode(
                            value
                        )
                        )
                    }
                    """.trimIndent(),
                )
                val layout =
                    repositoryKotlinStyle(
                        sourceFiles = listOf(sourceFile),
                    )

                // WHEN
                val violations = layout.inspect(sourceFile)
                val formatted =
                    layout.format(
                        file = sourceFile,
                    )

                // THEN
                violations.shouldNotBeEmpty()
                formatted shouldBe
                    canonicalKotlin(
                        source = """
                        fun collect(
                            results: MutableList<String>,
                            mapNode: (value: String) -> String,
                            value: String,
                        ) {
                            results.add(
                                element =
                                    mapNode(
                                        value,
                                    ),
                            )
                        }
                        """,
                    )
                sourceFile.writeText(formatted)
                layout.inspect(sourceFile).shouldBeEmpty()
            }

            "does not infer repository parameter names for qualified or receiver scope calls" {
                // GIVEN
                val sourceFile =
                    Files
                        .createTempFile(
                            "ReceiverCallLayout",
                            ".kt",
                        ).toFile()
                sourceFile.writeText(
                    """
                    fun add(
                        child: String
                    ) = Unit

                    fun collect(
                        children: MutableList<String>,
                        child: String
                    ) {
                        children.add(child)
                        children.apply { add(child) }
                    }
                    """.trimIndent(),
                )
                val layout =
                    repositoryKotlinStyle(
                        sourceFiles = listOf(sourceFile),
                    )

                // WHEN
                val formatted = layout.format(sourceFile)

                // THEN
                formatted shouldBe
                    canonicalKotlin(
                        source = """
                        fun add(
                            child: String,
                        ) = Unit

                        fun collect(
                            children: MutableList<String>,
                            child: String,
                        ) {
                            children.add(child)
                            children.apply { add(child) }
                        }
                        """,
                    )
            }

            "names recursive calls when their signature is unambiguous" {
                // GIVEN
                val sourceFile =
                    Files
                        .createTempFile(
                            "RecursiveCallLayout",
                            ".kt",
                        ).toFile()
                sourceFile.writeText(
                    """
                    fun check(
                        condition: Boolean,
                        message: String
                    ) {
                        check(
                            condition,
                            message
                        )
                    }
                    """.trimIndent(),
                )
                val layout =
                    repositoryKotlinStyle(
                        sourceFiles = listOf(sourceFile),
                    )

                // WHEN
                val formatted =
                    layout.format(
                        file = sourceFile,
                    )

                // THEN
                formatted shouldBe
                    canonicalKotlin(
                        source = """
                        fun check(
                            condition: Boolean,
                            message: String,
                        ) {
                            check(
                                condition = condition,
                                message = message,
                            )
                        }
                        """,
                    )
            }

            "does not infer named arguments for Kotlin Script DSL calls" {
                // GIVEN
                val sourceFile =
                    Files
                        .createTempFile(
                            "kotlin-script-call-layout",
                            ".kts",
                        ).toFile()
                sourceFile.writeText(
                    """
                    fun register(
                        name: String,
                        type: String
                    ) = Unit

                    register(
                        "check",
                        "verification"
                    )
                    """.trimIndent(),
                )
                val layout =
                    repositoryKotlinStyle(
                        sourceFiles = listOf(sourceFile),
                    )

                // WHEN
                val formatted =
                    layout.format(
                        file = sourceFile,
                    )

                // THEN
                formatted shouldBe
                    canonicalKotlin(
                        source = """
                        fun register(
                            name: String,
                            type: String,
                        ) = Unit

                        register(
                            "check",
                            "verification",
                        )
                        """,
                    )
            }

            "keeps short function type parameters inline inside a vertical declaration" {
                // GIVEN
                val sourceFile =
                    Files
                        .createTempFile(
                            "FunctionTypeParameterLayout",
                            ".kt",
                        ).toFile()
                sourceFile.writeText(
                    """
                    fun <T, R> traverse(
                        pathSegment: (
                            T
                        ) -> String,
                        mapNode: (
                            value: T,
                            fullPath: String
                        ) -> R
                    ) = Unit
                    """.trimIndent(),
                )
                val layout = repositoryKotlinStyle()

                // WHEN
                val formatted =
                    layout.format(
                        file = sourceFile,
                    )

                // THEN
                formatted shouldBe
                    canonicalKotlin(
                        source = """
                        fun <T, R> traverse(
                            pathSegment: (T) -> String,
                            mapNode: (value: T, fullPath: String) -> R,
                        ) = Unit
                        """,
                    )
                sourceFile.writeText(formatted)
                layout.inspect(sourceFile).shouldBeEmpty()
            }

            "wraps function type parameters when their containing line exceeds the repository limit" {
                // GIVEN
                val sourceFile =
                    Files
                        .createTempFile(
                            "LongFunctionTypeParameterLayout",
                            ".kt",
                        ).toFile()
                sourceFile.writeText(
                    """
                    fun transform(
                        mapper: (firstValue: ExtremelyLongDomainValueNameThatMakesTheSignatureExceedTheConfiguredMargin, secondValue: AnotherExtremelyLongDomainValueName) -> String
                    ) = Unit
                    """.trimIndent(),
                )
                val layout = repositoryKotlinStyle()

                // WHEN
                val formatted =
                    layout.format(
                        file = sourceFile,
                    )

                // THEN
                formatted shouldBe
                    canonicalKotlin(
                        source = """
                        fun transform(
                            mapper: (
                                firstValue: ExtremelyLongDomainValueNameThatMakesTheSignatureExceedTheConfiguredMargin,
                                secondValue: AnotherExtremelyLongDomainValueName,
                            ) -> String,
                        ) = Unit
                        """,
                    )
            }

            "reports violations before formatting and accepts the corrected source" {
                // GIVEN
                val sourceFile =
                    Files
                        .createTempFile(
                            "function-argument-layout-check",
                            ".kts",
                        ).toFile()
                sourceFile.writeText("register(\"check\", CheckTask::class.java)")
                val layout = repositoryKotlinStyle()

                // WHEN
                val violations = layout.inspect(sourceFile)
                sourceFile.writeText(
                    layout.format(
                        file = sourceFile,
                    ),
                )

                // THEN
                violations.shouldNotBeEmpty()
                layout.inspect(sourceFile).shouldBeEmpty()
            }

            "reports standard and repository-owned KtLint rules together" {
                // GIVEN
                val sourceFile =
                    Files
                        .createTempFile(
                            "CombinedKotlinStyle",
                            ".kt",
                        ).toFile()
                sourceFile.writeText("fun greet(name : String)=name")
                val style = repositoryKotlinStyle()

                // WHEN
                val ruleIds = style.inspect(sourceFile).map(RepositoryKotlinStyle.Violation::ruleId)

                // THEN
                ruleIds shouldContain "standard:colon-spacing"
                ruleIds shouldContain "water-my-plants:multiline-function-arguments"
            }
        },
    )

private fun repositoryKotlinStyle(
    sourceFiles: List<File> = emptyList(),
): RepositoryKotlinStyle =
    RepositoryKotlinStyle(
        sourceFiles = sourceFiles,
        editorConfigFile =
            File(
                checkNotNull(System.getProperty("waterMyPlants.repositoryRoot")),
                ".editorconfig",
            ),
    )

private fun canonicalKotlin(
    source: String,
): String = source.trimIndent() + "\n"
