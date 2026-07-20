---
title: Kotlin standard
type: standard
scope: repository
owner: android
status: active
last-reviewed: 2026-07-20
review-cycle-days: 180
sources:
  - .editorconfig
  - gradle.properties
  - repo/gradle-plugins/android
  - repo/gradle-plugins/unit-test
  - repo/verification-platform
---

# Kotlin Standard

## Source Design

- Code MUST use the formatter and compiler configuration supplied by the
  repository build. Do not introduce module-local formatting policy.
- Every parameter in a function declaration MUST appear on its own line with
  its type, including declarations with a single parameter. The closing
  parenthesis MUST also be on its own line. Empty declarations MAY remain
  inline.
- Parameter lists inside function types used for lambda-valued parameters MAY
  remain inline when the complete source line does not exceed the repository
  limit of 120 characters. For example,
  `(value: T, fullPath: String) -> R` remains inline; this exception does not
  change the vertical layout of the containing function declaration.
- Calls with two or more arguments MUST place every argument on its own line.
  Any named argument MUST also appear on its own line, even when it is the only
  argument. A single positional argument MAY remain inline only when Kotlin
  does not support a named argument for that call.
- Every multiline parameter or argument list MUST use one indentation level
  relative to its own declaration or call. In a nested call, indent against the
  nested call rather than the outer argument list, and align each closing
  parenthesis with the declaration or call that opened it.
- Every argument passed to a Kotlin function or constructor MUST use its
  parameter name when Kotlin permits named arguments. This applies even when
  the value or single-argument API appears self-explanatory. For example,
  `visit(node = child, path = currentPath)` is required instead of relying on
  positional order.
- Positional arguments are permitted only at call sites where Kotlin rejects
  named arguments, including Java callables, function-value or lambda
  invocation, and individual `vararg` elements. Gradle Kotlin DSL and other
  external APIs MUST follow their compiler-visible contract; do not invent
  parameter names for Java or generated callables.
- `checkKotlinFunctionArguments` automatically resolves and enforces named
  arguments for unambiguous Kotlin declarations in the same `.kt` source file.
  Cross-file calls, receiver calls, external APIs, and Kotlin Script DSL calls
  still require compiler validation and code review because the KtLint-based
  formatter does not perform Kotlin type resolution.
- Apply the same layout to Kotlin source and Kotlin Script files. The root
  `.editorconfig` configures compatible IDE wrapping; the executable Gradle
  check remains authoritative for repository and CI verification.
- Prefer immutable values and pure transformations. Mutable state MUST have a
  clear owner and lifecycle.
- Public names MUST express domain intent. Avoid generic names such as
  `Manager`, `Helper`, or `Utils` when a capability name is available.
- Production failures MUST use typed results or domain exceptions at module
  boundaries. Do not use `null` to hide an operational failure.
- Do not catch `Throwable`, cancellation exceptions, or broad exceptions
  without rethrowing cancellation and translating the remaining failure.

## Types And APIs

- Keep one primary public top-level type per file. Private helpers and tightly
  owned nested types may remain with their owner.
- Prefer sealed hierarchies for closed state or result sets.
- Default to `internal`; make an API public only for an identified consumer.
- Extension functions MUST remain close to the type or capability they extend.
  Unrelated catch-all extension files are prohibited.

## Coroutines

- Coroutines MUST use structured concurrency and an injected or caller-owned
  scope. `GlobalScope` is prohibited.
- Dispatchers MUST be supplied at infrastructure boundaries when deterministic
  tests require control.
- Flows exposed as state MUST have a single state owner. Callers should receive
  read-only `Flow` or `StateFlow` views.
- Cancellation MUST propagate across suspend boundaries.

## Verification

Run the focused module test task during implementation and `./gradlew check`
before merge. The root `check` lifecycle includes
`checkKotlinFunctionArguments`, which rejects nonconforming `.kt` and `.kts`
files. Run `./gradlew formatKotlinFunctionArguments` to migrate or correct
source files automatically.

## Sources

- `.editorconfig`
- `repo/gradle-plugins/android/`
- `repo/gradle-plugins/unit-test/`
- `repo/verification-platform/`
