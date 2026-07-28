package com.marmatsan.unitTest.dsl.phase

/**
 * Holds an arrangement and its action result until a scenario verifies the outcome.
 *
 * Instances are created by `whenever`; the constructor is internal so callers
 * cannot perform assertions without first evaluating the action.
 *
 * @param GivenValue Type produced by the arrangement.
 * @param WhenValue Type produced by the action.
 * @property givenValue Arrangement retained for assertions that need scenario context.
 * @property value Action result supplied to the assertion.
 */
class WhenPhase<GivenValue, WhenValue> internal constructor(
    private val givenValue: GivenValue,
    private val value: WhenValue,
) {
    /**
     * Evaluates the scenario assertions once with the action result.
     *
     * Assertion failures propagate unchanged to preserve the test framework's
     * failure type, message, and stack trace.
     *
     * @param block Suspended assertions over the observable action result.
     */
    suspend fun then(
        block: suspend (WhenValue) -> Unit,
    ) {
        block(value)
    }

    /**
     * Evaluates assertions that need both the arrangement and the action result.
     *
     * This overload keeps scenario context typed without shared mutable state or
     * artificial outcome wrappers. Assertion failures propagate unchanged.
     *
     * @param block Suspended assertions receiving the arrangement followed by the result.
     */
    suspend fun then(
        block: suspend (GivenValue, WhenValue) -> Unit,
    ) {
        block(
            givenValue,
            value,
        )
    }
}
