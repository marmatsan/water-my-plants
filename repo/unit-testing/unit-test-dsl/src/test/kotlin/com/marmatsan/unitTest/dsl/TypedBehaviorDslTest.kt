package com.marmatsan.unitTest.dsl

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class TypedBehaviorDslTest :
    FunSpec(
        {
            test("passes typed values through the phases in order exactly once") {
                val events = mutableListOf<String>()
                given {
                    events += "given"
                    20
                }.whenever { value ->
                    events += "whenever"
                    value + 22
                }.then { result ->
                    events += "then"
                    result shouldBe 42
                    events.shouldContainExactly(
                        "given",
                        "whenever",
                        "then",
                    )
                }
            }

            test("supports suspended work in every phase") {
                given {
                    suspendedValue(
                        value = 20,
                    )
                }.whenever { value ->
                    suspendedValue(
                        value = value + 22,
                    )
                }.then { result ->
                    suspendedValue(
                        value = result,
                    ) shouldBe 42
                }
            }

            test("provides arrangement and action result to contextual assertions") {
                given {
                    mutableListOf("given")
                }.whenever { fixture ->
                    fixture + "whenever"
                }.then { fixture, result ->
                    fixture.shouldContainExactly("given")
                    result.shouldContainExactly(
                        "given",
                        "whenever",
                    )
                }
            }

            test("does not run later phases when the arrangement fails") {
                given {
                    mutableListOf<String>()
                }.whenever { events ->
                    val expected =
                        ScenarioFailure(
                            message = "arrangement failed",
                        )
                    val actual =
                        shouldThrow<ScenarioFailure> {
                            given<Int> {
                                events += "given"
                                throw expected
                            }.whenever { value ->
                                events += "whenever"
                                value
                            }.then {
                                events += "then"
                            }
                        }
                    FailureOutcome(
                        events = events,
                        expected = expected,
                        actual = actual,
                    )
                }.then { outcome ->
                    outcome.events.shouldContainExactly("given")
                    outcome.actual shouldBeSameInstanceAs outcome.expected
                }
            }

            test("does not run assertions when the action fails") {
                given {
                    mutableListOf<String>()
                }.whenever { events ->
                    val expected =
                        ScenarioFailure(
                            message = "action failed",
                        )
                    val actual =
                        shouldThrow<ScenarioFailure> {
                            given {
                                events += "given"
                                Unit
                            }.whenever {
                                events += "whenever"
                                throw expected
                            }.then {
                                events += "then"
                            }
                        }
                    FailureOutcome(
                        events = events,
                        expected = expected,
                        actual = actual,
                    )
                }.then { outcome ->
                    outcome.events.shouldContainExactly(
                        "given",
                        "whenever",
                    )
                    outcome.actual shouldBeSameInstanceAs outcome.expected
                }
            }

            test("does not share scenario state") {
                given {
                    listOf(
                        mutableListOf<String>(),
                        mutableListOf<String>(),
                    )
                }.whenever { scenarios ->
                    scenarios.forEachIndexed { index, events ->
                        given {
                            events += "scenario-$index"
                            events
                        }.whenever { it.toList() }
                            .then { snapshot -> snapshot shouldBe listOf("scenario-$index") }
                    }
                    scenarios
                }.then { scenarios ->
                    scenarios[0].shouldContainExactly("scenario-0")
                    scenarios[1].shouldContainExactly("scenario-1")
                }
            }
        },
    )

private suspend fun <Value> suspendedValue(
    value: Value,
): Value =
    suspendCoroutine { continuation ->
        continuation.resume(value)
    }

private data class FailureOutcome(
    val events: List<String>,
    val expected: ScenarioFailure,
    val actual: ScenarioFailure,
)

private class ScenarioFailure(
    message: String,
) : RuntimeException(message)
