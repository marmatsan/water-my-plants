---
title: Add an API endpoint
type: guide
scope: product-data
owner: data
status: active
last-reviewed: 2026-07-18
review-cycle-days: 180
sources:
  - docs/standards/api-client.md
  - docs/standards/architecture.md
  - repo/dependency-catalog/versions.properties
---

# Add An API Endpoint

## Outcome

Add one remote capability while keeping HTTP and serialization details inside
the data adapter.

## Applicable Standards

Read the [API client](../standards/api-client.md),
[architecture](../standards/architecture.md), [Kotlin](../standards/kotlin.md),
and [testing](../standards/testing.md) standards.

## Steps

1. If this is the first production endpoint, create an ADR selecting the HTTP
   and serialization stack before adding dependencies.
2. Define the domain-facing operation and its typed success and failure model.
3. Add request and response DTOs inside the transport adapter.
4. Map DTOs to domain models explicitly.
5. Configure the endpoint through the shared client; do not create an ad hoc
   client instance in a repository or UI class.
6. Translate protocol, connectivity, timeout, and malformed-response failures
   at the adapter boundary while preserving cancellation.
7. Add deterministic adapter tests for success and each relevant failure.
8. Register new dependency versions and aliases through the repository catalog
   if the selected stack requires them.

## Verification

Run adapter tests, catalog naming and usage checks, and `./gradlew check`.
Inspect logs to ensure credentials and sensitive payloads are absent.

## Related Documentation

- `docs/templates/adr.md`
- `repo/figma-design-sync/docs/standards/dependency-version-naming.md`
