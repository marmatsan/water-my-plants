---
title: API client standard
type: standard
scope: product-data
owner: data
status: active
last-reviewed: 2026-08-06
review-cycle-days: 180
sources:
  - docs/decisions/adr-0016-use-ktor-and-kotlin-serialization-for-product-apis.md
  - docs/standards/architecture.md
  - docs/reference/product-technology-stack.md
  - versions.properties
---

# API Client Standard

## Selected Stack

Product API adapters use Ktor Client with Kotlin Serialization JSON as selected
by
[ADR-0016](../decisions/adr-0016-use-ktor-and-kotlin-serialization-for-product-apis.md).
The first production endpoint activates the root product aliases and chooses
the Android-compatible Ktor engine. Included-build Ktor dependencies under
`repo/` are implementation evidence, not product catalog ownership.

## Transport Isolation

- HTTP client, response, request, and serialization types MUST remain inside a
  data or transport adapter.
- Domain and UI modules MUST depend on capability interfaces and domain models,
  not URLs, status codes, headers, or transport DTOs.
- DTO-to-domain mapping MUST be explicit and tested.
- Endpoint paths, base URLs, authentication, timeouts, and retry policy MUST be
  configured centrally for a client instance.

## Serialization

- Request and response DTOs use Kotlin Serialization and remain inside the
  transport adapter.
- One centrally configured `Json` instance owns unknown-key, default,
  nullability, and enum-evolution behavior for an API boundary.
- Protocol Buffers is the selected Proto DataStore schema format. An API uses
  it instead of JSON only when the server contract explicitly requires it.

## Errors And Cancellation

- Transport failures MUST be translated into a small typed error contract at
  the adapter boundary.
- Cancellation MUST never be converted into a network failure.
- Retries MUST be bounded, observable, and limited to operations known to be
  safe to retry. UI code MUST NOT implement retry loops around a client.
- Empty, partial, or malformed responses MUST have explicit handling.

## Security

- Tokens, authorization headers, cookies, and sensitive bodies MUST NOT be
  logged.
- Secrets MUST come from an approved runtime or CI secret source, never source
  control.
- Certificate and cleartext exceptions require an ADR and a documented threat
  assessment.

## Verification

Adapter tests MUST cover successful mapping, protocol failures, malformed
payloads, cancellation, and retry limits. Contract tests SHOULD use a local
deterministic server rather than the real service.

## Sources

- `docs/decisions/adr-0016-use-ktor-and-kotlin-serialization-for-product-apis.md`
- `docs/standards/architecture.md`
- `docs/reference/product-technology-stack.md`
- `versions.properties`
