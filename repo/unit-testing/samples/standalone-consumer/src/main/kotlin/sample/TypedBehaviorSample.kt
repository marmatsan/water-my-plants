package sample

import com.marmatsan.unitTest.dsl.given

/** Compiles the public typed behavior API without relying on a source composite. */
suspend fun verifyTypedBehaviorDsl() {
    given {
        2
    }.whenever { value ->
        value * 2
    }.then { result ->
        check(result == 4)
    }
}
