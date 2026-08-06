---
title: API client standard
type: standard
scope: product-data
owner: data
status: active
last-reviewed: 2026-07-18
review-cycle-days: 180
sources:
  - docs/standards/architecture.md
  - versions.properties
---

# API Client Standard

## Current Boundary

The repository has not selected an HTTP client or serialization stack for
production APIs. That selection MUST be recorded in an ADR when the first real
endpoint is implemented. This standard defines the boundary independently of
that library choice.

## Transport Isolation

- HTTP client, response, request, and serialization types MUST remain inside a
  data or transport adapter.
- Domain and UI modules MUST depend on capability interfaces and domain models,
  not URLs, status codes, headers, or transport DTOs.
- DTO-to-domain mapping MUST be explicit and tested.
- Endpoint paths, base URLs, authentication, timeouts, and retry policy MUST be
  configured centrally for a client instance.

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

- `docs/standards/architecture.md`
- `versions.properties`
