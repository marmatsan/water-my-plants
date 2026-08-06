---
title: Use Room and Proto DataStore for product persistence
type: adr
scope: product-data
owner: data
status: accepted
last-reviewed: 2026-08-06
review-cycle-days: 365
sources:
  - docs/standards/architecture.md
  - docs/reference/product-technology-stack.md
  - versions.properties
  - settings.gradle.kts
---

# ADR-0015: Use Room And Proto DataStore For Product Persistence

## Context

Water My Plants needs two different forms of local persistence. Plant records
require structured queries, relationships, transactions, and explicit schema
migrations. Small application settings require one observable, typed value
whose schema can evolve without introducing relational tables.

The persistence boundary must preserve domain independence, user data, typed
failure semantics, and deterministic migration tests. The legacy application
used Realm, but that implementation does not define the architecture of the
current product.

## Decision

- Use Room for relational product data such as plants, watering records, and
  other entities that require queries, relationships, or transactions.
- Use AndroidX Proto DataStore for small typed application settings. Proto
  DataStore means `DataStore<T>` backed by an owned Protocol Buffers schema;
  Preferences DataStore is not the selected default.
- Keep Room entities, DAOs, DataStore serializers, generated Protocol Buffers
  messages, and migration implementations inside data adapters. Domain-facing
  ports and models remain independent of AndroidX and generated types.
- The `:app` composition root provides one Room database instance per database
  and one DataStore instance per file. Feature UI and domain code do not create
  or locate persistence instances.
- Add Room and DataStore versions, aliases, compiler configuration, and module
  dependencies to the root product catalog with their first production
  consumers. Room schema generation uses the project's KSP foundation.
- Every Room schema change increments the database version and supplies an
  explicit, deterministic migration test. Release builds must not use a
  destructive migration fallback for product data.
- Protocol Buffers field numbers are stable and never reused. New fields have
  backward-compatible defaults, and migrations from an earlier settings store
  are explicit, idempotent, and tested.
- Translate expected I/O, corruption, and migration failures into the
  capability-owned `kotlin-result` error contract. Preserve coroutine
  cancellation and do not silently replace unknown or unreadable production
  state.
- A corruption handler may replace DataStore content only when the owning
  capability declares the value safely reconstructable and tests the recovery
  result. Plant data is never treated as disposable production state.
- This decision selects Protocol Buffers for Proto DataStore schemas. Using
  Protocol Buffers as a remote transport format still requires an endpoint
  contract that demonstrates that need.

## Consequences

- Relational product data receives Room's compile-time query validation,
  transactions, observable queries, and explicit schema migration surface.
- Settings remain typed and observable without using relational storage for a
  single configuration document.
- Persistence adapters incur explicit mapping and migration code, preventing
  vendor models from becoming domain models.
- Room and DataStore do not provide application-level encryption by this
  decision. Persisting credentials or sensitive personal data requires a
  threat assessment and an approved storage decision before implementation.
- The root catalog will gain runtime and compiler dependencies only when
  checked-in production code consumes them.

## Alternatives

- Realm was not retained because the current product does not need to inherit
  a legacy vendor model, migration surface, or domain coupling.
- SQLDelight was not selected because Room provides the Android-first
  relational integration and KSP workflow required by the current product.
- Preferences DataStore was not selected as the default because the approved
  settings contract is typed and schema-owned.
- JSON files or shared preferences were rejected because they would require a
  repository-owned concurrency, atomicity, serialization, and migration
  contract.
- Room for small settings was rejected because relational tables add no value
  to one typed configuration document.

## Supersession

None.
