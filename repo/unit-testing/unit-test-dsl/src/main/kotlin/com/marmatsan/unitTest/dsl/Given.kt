package com.marmatsan.unitTest.dsl

import com.marmatsan.unitTest.dsl.phase.GivenPhase

/**
 * Evaluates the arrangement for a test scenario and starts a typed behavior chain.
 *
 * The returned phase retains only the value produced by [block]. Failures are not
 * wrapped, so the test engine reports the original exception and stack trace.
 *
 * @param block Suspended arrangement that produces the value required by the action.
 * @return A phase that exposes only the transition to the action.
 */
suspend fun <GivenValue> given(
    block: suspend () -> GivenValue
): GivenPhase<GivenValue> =
    GivenPhase(
        value = block()
    )
