package com.marmatsan.verificationPlatform.domain.service.errorhandling

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.fold
import com.marmatsan.unitTest.dsl.given
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

internal class TypedResultUsageValidatorTest :
    FunSpec(
        {
            test("accepts the configured Result without an alias") {
                given {
                    """
                    package example

                    import com.github.michaelbull.result.Result

                    fun load(): Result<String, LoadError> = TODO()
                    """.trimIndent()
                }.whenever { source ->
                    validator().validate(
                        relativePath = "app/src/main/kotlin/example/Loader.kt",
                        source = source,
                    )
                }.then { violations ->
                    violations shouldBe Ok(Unit)
                }
            }

            test("ignores incompatible Result names in comments and literals") {
                given {
                    listOf(
                        "val reference = \"other.library.Result<String, Error>\"",
                        "val character = 'R'",
                        "// import kotlin.Result",
                        "/*",
                        "sealed interface Result<out Value, out Error>",
                        "*/",
                        "val raw = \"\"\"",
                        "import example.Result",
                        "\"\"\"",
                    ).joinToString(
                        separator = "\n",
                    )
                }.whenever { source ->
                    validator().validate(
                        relativePath = "app/src/main/kotlin/example/Description.kt",
                        source = source,
                    )
                }.then { result ->
                    result shouldBe Ok(Unit)
                }
            }

            test("rejects standard library, aliased, foreign, and custom Result contracts") {
                given {
                    """
                    import kotlin.Result
                    import com.github.michaelbull.result.Result as Outcome
                    import example.Result

                    sealed interface Result<out Value, out Error>
                    typealias Result = kotlin.Result<String>
                    val qualified: other.library.Result<String, Error> = TODO()
                    """.trimIndent()
                }.whenever { source ->
                    validator().validate(
                        relativePath = "app/src/main/kotlin/example/ResultContracts.kt",
                        source = source,
                    )
                }.then { violations ->
                    violations
                        .fold(
                            { error("Expected incompatible Result contracts to fail validation.") },
                            { failure -> failure.violations },
                        ).map { violation -> violation.lineNumber to violation.reason }
                        .shouldContainExactly(
                            1 to
                                "must import com.github.michaelbull.result.Result instead of " +
                                "kotlin.Result",
                            2 to
                                "must import com.github.michaelbull.result.Result without an alias",
                            3 to
                                "must import com.github.michaelbull.result.Result instead of " +
                                "example.Result",
                            5 to
                                "must not declare a custom Result; use " +
                                "com.github.michaelbull.result.Result",
                            6 to
                                "must not declare a custom Result; use " +
                                "com.github.michaelbull.result.Result",
                            7 to
                                "must use com.github.michaelbull.result.Result instead of " +
                                "other.library.Result",
                        )
                }
            }
        },
    )

private fun validator(): TypedResultUsageValidator =
    TypedResultUsageValidator(
        acceptedResultQualifiedName = "com.github.michaelbull.result.Result",
    )
