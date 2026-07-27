package com.marmatsan.verificationPlatform.data.kotlin

import com.marmatsan.unitTest.dsl.given
import io.kotest.core.spec.style.StringSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import java.io.File

class RepositoryKotlinStyleTest :
    StringSpec(
        {
            val temporaryDirectory =
                tempdir(
                    prefix = "repository-kotlin-style",
                )

            "formats every declaration parameter and multiple call arguments vertically" {
                given {
                    val sourceFile =
                        temporaryDirectory.resolve("FunctionArgumentLayout.kt")
                    sourceFile.writeText(
                        """
                        fun greet(first: String, second: String) = combine(first, second)

                        fun printSingle(value: String) = println(value)
                        """.trimIndent(),
                    )
                    FormattingFixture(
                        sourceFile = sourceFile,
                        style = repositoryKotlinStyle(),
                    )
                }.whenever { fixture ->
                    fixture.style.format(
                        file = fixture.sourceFile,
                    )
                }.then { formatted ->
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
            }

            "formats a single named argument vertically but leaves a clear positional argument inline" {
                given {
                    val sourceFile =
                        temporaryDirectory.resolve("NamedFunctionArgumentLayout.kt")
                    sourceFile.writeText(
                        """
                        fun render(label: String) = Unit

                        fun show(value: String) {
                            render(label = value)
                            println(value)
                        }
                        """.trimIndent(),
                    )
                    FormattingFixture(
                        sourceFile = sourceFile,
                        style = repositoryKotlinStyle(),
                    )
                }.whenever { fixture ->
                    fixture.style.format(
                        file = fixture.sourceFile,
                    )
                }.then { fixture, formatted ->
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
                    fixture.sourceFile.writeText(formatted)
                    fixture.style.inspect(fixture.sourceFile).shouldBeEmpty()
                }
            }

            "names arguments for repository Kotlin functions and constructors" {
                given {
                    val sourceFile =
                        temporaryDirectory.resolve("NamedRepositoryCallLayout.kt")
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
                    FormattingFixture(
                        sourceFile = sourceFile,
                        style =
                            repositoryKotlinStyle(
                                sourceFiles = listOf(sourceFile),
                            ),
                    )
                }.whenever { fixture ->
                    FormattingResult(
                        violations = fixture.style.inspect(fixture.sourceFile),
                        formatted =
                            fixture.style.format(
                                file = fixture.sourceFile,
                            ),
                    )
                }.then { fixture, result ->
                    result.violations.shouldNotBeEmpty()
                    result.formatted shouldBe
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
                    fixture.sourceFile.writeText(result.formatted)
                    fixture.style.inspect(fixture.sourceFile).shouldBeEmpty()
                }
            }

            "leaves function value invocations positional because Kotlin does not support names" {
                given {
                    val sourceFile =
                        temporaryDirectory.resolve("FunctionValueCallLayout.kt")
                    sourceFile.writeText(
                        """
                        fun transform(
                            mapNode: (value: String, fullPath: String) -> String,
                            value: String,
                            fullPath: String
                        ) = mapNode(value, fullPath)
                        """.trimIndent(),
                    )
                    FormattingFixture(
                        sourceFile = sourceFile,
                        style =
                            repositoryKotlinStyle(
                                sourceFiles = listOf(sourceFile),
                            ),
                    )
                }.whenever { fixture ->
                    fixture.style.format(
                        file = fixture.sourceFile,
                    )
                }.then { formatted ->
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
            }

            "indents nested function arguments relative to their own call" {
                given {
                    val sourceFile =
                        temporaryDirectory.resolve("NestedFunctionArgumentLayout.kt")
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
                    FormattingFixture(
                        sourceFile = sourceFile,
                        style =
                            repositoryKotlinStyle(
                                sourceFiles = listOf(sourceFile),
                            ),
                    )
                }.whenever { fixture ->
                    FormattingResult(
                        violations = fixture.style.inspect(fixture.sourceFile),
                        formatted =
                            fixture.style.format(
                                file = fixture.sourceFile,
                            ),
                    )
                }.then { fixture, result ->
                    result.violations.shouldNotBeEmpty()
                    result.formatted shouldBe
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
                    fixture.sourceFile.writeText(result.formatted)
                    fixture.style.inspect(fixture.sourceFile).shouldBeEmpty()
                }
            }

            "does not infer repository parameter names for qualified or receiver scope calls" {
                given {
                    val sourceFile =
                        temporaryDirectory.resolve("ReceiverCallLayout.kt")
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
                    FormattingFixture(
                        sourceFile = sourceFile,
                        style =
                            repositoryKotlinStyle(
                                sourceFiles = listOf(sourceFile),
                            ),
                    )
                }.whenever { fixture ->
                    fixture.style.format(fixture.sourceFile)
                }.then { formatted ->
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
            }

            "names recursive calls when their signature is unambiguous" {
                given {
                    val sourceFile =
                        temporaryDirectory.resolve("RecursiveCallLayout.kt")
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
                    FormattingFixture(
                        sourceFile = sourceFile,
                        style =
                            repositoryKotlinStyle(
                                sourceFiles = listOf(sourceFile),
                            ),
                    )
                }.whenever { fixture ->
                    fixture.style.format(
                        file = fixture.sourceFile,
                    )
                }.then { formatted ->
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
            }

            "does not infer named arguments for Kotlin Script DSL calls" {
                given {
                    val sourceFile =
                        temporaryDirectory.resolve("kotlin-script-call-layout.kts")
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
                    FormattingFixture(
                        sourceFile = sourceFile,
                        style =
                            repositoryKotlinStyle(
                                sourceFiles = listOf(sourceFile),
                            ),
                    )
                }.whenever { fixture ->
                    fixture.style.format(
                        file = fixture.sourceFile,
                    )
                }.then { formatted ->
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
            }

            "keeps short function type parameters inline inside a vertical declaration" {
                given {
                    val sourceFile =
                        temporaryDirectory.resolve("FunctionTypeParameterLayout.kt")
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
                    FormattingFixture(
                        sourceFile = sourceFile,
                        style = repositoryKotlinStyle(),
                    )
                }.whenever { fixture ->
                    fixture.style.format(
                        file = fixture.sourceFile,
                    )
                }.then { fixture, formatted ->
                    formatted shouldBe
                        canonicalKotlin(
                            source = """
                            fun <T, R> traverse(
                                pathSegment: (T) -> String,
                                mapNode: (value: T, fullPath: String) -> R,
                            ) = Unit
                            """,
                        )
                    fixture.sourceFile.writeText(formatted)
                    fixture.style.inspect(fixture.sourceFile).shouldBeEmpty()
                }
            }

            "wraps function type parameters when their containing line exceeds the repository limit" {
                given {
                    val sourceFile =
                        temporaryDirectory.resolve("LongFunctionTypeParameterLayout.kt")
                    sourceFile.writeText(
                        """
                        fun transform(
                            mapper: (firstValue: ExtremelyLongDomainValueNameThatMakesTheSignatureExceedTheConfiguredMargin, secondValue: AnotherExtremelyLongDomainValueName) -> String
                        ) = Unit
                        """.trimIndent(),
                    )
                    FormattingFixture(
                        sourceFile = sourceFile,
                        style = repositoryKotlinStyle(),
                    )
                }.whenever { fixture ->
                    fixture.style.format(
                        file = fixture.sourceFile,
                    )
                }.then { formatted ->
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
            }

            "reports violations before formatting and accepts the corrected source" {
                given {
                    val sourceFile =
                        temporaryDirectory.resolve("function-argument-layout-check.kts")
                    sourceFile.writeText("register(\"check\", CheckTask::class.java)")
                    FormattingFixture(
                        sourceFile = sourceFile,
                        style = repositoryKotlinStyle(),
                    )
                }.whenever { fixture ->
                    fixture.style.inspect(fixture.sourceFile).also {
                        fixture.sourceFile.writeText(
                            fixture.style.format(
                                file = fixture.sourceFile,
                            ),
                        )
                    }
                }.then { fixture, violations ->
                    violations.shouldNotBeEmpty()
                    fixture.style.inspect(fixture.sourceFile).shouldBeEmpty()
                }
            }

            "reports standard and repository-owned KtLint rules together" {
                given {
                    val sourceFile =
                        temporaryDirectory.resolve("CombinedKotlinStyle.kt")
                    sourceFile.writeText("fun greet(name : String)=name")
                    FormattingFixture(
                        sourceFile = sourceFile,
                        style = repositoryKotlinStyle(),
                    )
                }.whenever { fixture ->
                    fixture.style.inspect(fixture.sourceFile).map(RepositoryKotlinStyle.Violation::ruleId)
                }.then { ruleIds ->
                    ruleIds shouldContain "standard:colon-spacing"
                    ruleIds shouldContain "repository-verification:multiline-function-arguments"
                }
            }

            "requires typed behavior phases instead of legacy section comments" {
                given {
                    listOf(
                        "GIVEN",
                        "WHEN",
                        "THEN",
                    )
                }.whenever { markers ->
                    markers.map { marker ->
                        val sourceFile =
                            temporaryDirectory.resolve("TypedBehavior$marker.kt")
                        sourceFile.writeText(
                            listOf(
                                "fun behavior() {",
                                "    " + "// " + marker,
                                "    Unit",
                                "}",
                            ).joinToString(
                                separator = "\n",
                            ),
                        )
                        repositoryKotlinStyle()
                            .inspect(sourceFile)
                            .map(RepositoryKotlinStyle.Violation::ruleId)
                    }
                }.then { ruleIdsByMarker ->
                    ruleIdsByMarker.forEach { ruleIds ->
                        ruleIds shouldContain "repository-verification:typed-behavior-sections"
                    }
                }
            }
        },
    )

private data class FormattingFixture(
    val sourceFile: File,
    val style: RepositoryKotlinStyle,
)

private data class FormattingResult(
    val violations: List<RepositoryKotlinStyle.Violation>,
    val formatted: String,
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
