---
title: Add an API endpoint
type: guide
scope: product-data
owner: data
status: active
last-reviewed: 2026-08-06
review-cycle-days: 180
sources:
  - docs/decisions/adr-0016-use-ktor-and-kotlin-serialization-for-product-apis.md
  - docs/standards/product-design.md
  - docs/standards/api-client.md
  - docs/standards/architecture.md
  - versions.properties
---

# Add An API Endpoint

## Outcome

Add one remote capability while keeping HTTP and serialization details inside
the data adapter.

## Applicable Standards

Read the [API client](../standards/api-client.md),
[architecture](../standards/architecture.md), [Kotlin](../standards/kotlin.md),
and [testing](../standards/testing.md) standards.
When the endpoint supports a user action, also apply the approved OOUX and BDD
contract from the [product design standard](../standards/product-design.md).

## Steps

1. Confirm the consumer-owned operation, success, expected failure, recovery,
   and network side effects from the approved action contract. If there is no
   endpoint contract yet, stop at design and do not add speculative transport
   source or dependencies.
2. Use the Ktor Client and Kotlin Serialization JSON stack selected by ADR-0016.
   With the first production endpoint, add its product-owned versions and
   aliases and select the Android-compatible Ktor engine.
3. Define the domain-facing operation and its typed success and failure model.
4. Add request and response DTOs inside the transport adapter.
5. Map DTOs to domain models explicitly.
6. Configure the endpoint through the shared client; do not create an ad hoc
   client instance in a repository or UI class.
7. Translate protocol, connectivity, timeout, and malformed-response failures
   at the adapter boundary while preserving cancellation.
8. Add deterministic adapter tests for success and each relevant failure.
9. Register new dependency versions and aliases through the repository catalog
   if the selected stack requires them.

## Verification

Run adapter tests, catalog naming and usage checks, and `./gradlew check`.
Inspect logs to ensure credentials and sensitive payloads are absent.

## Related Documentation

- [ADR-0016](../decisions/adr-0016-use-ktor-and-kotlin-serialization-for-product-apis.md)
- `repo/figma-documentation-sync/docs/standards/dependency-version-naming.md`
