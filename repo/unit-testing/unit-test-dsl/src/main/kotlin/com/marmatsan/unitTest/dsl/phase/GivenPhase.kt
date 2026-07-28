package com.marmatsan.unitTest.dsl.phase

/**
 * Holds the arrangement value until a scenario performs its action.
 *
 * Instances are created by `given`; the constructor is internal so callers cannot
 * bypass the first phase of the behavior chain.
 *
 * @param GivenValue Type produced by the arrangement.
 * @property value Arrangement value supplied to the action.
 */
class GivenPhase<GivenValue> internal constructor(
    private val value: GivenValue,
) {
    /**
     * Evaluates the action once with the arrangement value.
     *
     * `whenever` is used instead of `when` because `when` is a Kotlin keyword.
     * Failures propagate unchanged and prevent the assertion phase from running.
     *
     * @param block Suspended action that transforms the arrangement into an observable result.
     * @return A phase that exposes only assertions over the action result.
     */
    suspend fun <WhenValue> whenever(
        block: suspend (GivenValue) -> WhenValue,
    ): WhenPhase<GivenValue, WhenValue> =
        WhenPhase(
            givenValue = value,
            value = block(value),
        )
}
