---
title: Use Ktor and Kotlin Serialization for product APIs
type: adr
scope: product-data
owner: data
status: accepted
last-reviewed: 2026-08-06
review-cycle-days: 365
sources:
  - docs/standards/api-client.md
  - docs/standards/architecture.md
  - docs/reference/product-technology-stack.md
  - versions.properties
  - settings.gradle.kts
---

# ADR-0016: Use Ktor And Kotlin Serialization For Product APIs

## Context

The first production API adapter needs an HTTP client and serialization stack
that supports coroutines, structured configuration, testable request pipelines,
and explicit transport models. The selection must not leak HTTP, serialization,
or generated types into domain and UI contracts.

Repository tooling already demonstrates Ktor and Kotlin Serialization, but its
included-build catalogs are autonomous and cannot become product runtime
dependencies. The product must own its aliases and versions when its first API
consumer is implemented.

## Decision

- Use Ktor Client for production HTTP APIs and Kotlin Serialization for JSON
  request and response DTOs.
- Keep one centrally configured `HttpClient` per API boundary. Configure its
  engine, base URL, content negotiation, timeouts, authentication, logging, and
  retry policy at the data-adapter composition boundary.
- Select the Android-compatible Ktor engine with the first endpoint according
  to its runtime and test requirements. Engine types remain an adapter detail
  and do not alter the domain-facing port.
- Keep `@Serializable` DTOs inside the transport adapter and map them explicitly
  to domain models. Domain and UI APIs do not expose Ktor requests, responses,
  status codes, or serialization types.
- Centralize Kotlin Serialization `Json` options. Compatibility choices such
  as unknown-key handling, defaults, nullability, and enum evolution are
  intentional and covered by adapter contract tests.
- Translate expected connectivity, timeout, protocol, authentication, and
  malformed-payload failures into capability-owned errors returned with
  `kotlin-result`. Coroutine cancellation is never converted into a transport
  failure.
- Add Ktor, Kotlin Serialization, and the serialization plugin to the root
  product catalog and consuming module only with the first production endpoint.
  Do not reuse coordinates from an included build under `repo/`.
- JSON is the default API representation selected here. Protocol Buffers is
  used for Proto DataStore by ADR-0015 and becomes a remote representation only
  when a server endpoint explicitly requires it.

## Consequences

- API adapters share one coroutine-native client and one explicit JSON policy.
- Transport evolution is tested at the boundary and cannot silently change a
  domain contract.
- Client plugins and engines can vary behind the adapter without changing
  feature or domain modules.
- The initial endpoint must add deterministic tests with a local server for
  success, protocol failures, malformed content, cancellation, and configured
  retry limits.
- Product versions remain independent of repository-tooling versions and enter
  the product catalog only when consumed.

## Alternatives

- Retrofit with OkHttp was not selected because Ktor provides the coroutine-
  native, configurable client surface chosen for the product without generated
  service interfaces.
- Raw OkHttp or Android platform clients were rejected because they would
  require more repository-owned request, serialization, and pipeline code.
- Gson and Moshi were not selected because Kotlin Serialization provides the
  Kotlin-owned DTO and plugin integration selected for navigation and product
  schemas.
- Protocol Buffers for every API was rejected because representation is also a
  server contract; the current API default is JSON.

## Supersession

None.
