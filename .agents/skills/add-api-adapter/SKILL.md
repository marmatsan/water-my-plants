---
name: add-api-adapter
description: Add or materially change one Water My Plants API or transport adapter behind a consumer-owned domain boundary with explicit DTO mapping, typed errors, cancellation, security, contract tests, and documentation. Use for endpoint or remote-data integration work, including the first approved client selection.
---

# Add API Adapter

1. Read the root and scoped `AGENTS.md` files and the active specification.
2. Read the
   [API endpoint guide](../../../docs/guides/add-api-endpoint.md), the
   [API client standard](../../../docs/standards/api-client.md), the
   [typed-error standard](../../../docs/standards/error-handling.md), and the
   architecture and testing sources selected by the
   [context map](../../../docs/reference/code-generation-context.md).
3. Inspect the consumer-owned capability boundary, current domain models,
   existing adapters, configuration ownership, and failure semantics.
4. If this is the first production client or serialization selection, record
   and approve the required ADR before adding the technology.
5. Define transport DTOs and explicit mapping inside the adapter boundary.
   Preserve cancellation and translate recoverable infrastructure failure into
   the consumer-owned typed error contract.
6. Centralize client configuration and keep credentials and sensitive payloads
   outside source, logs, and test output.
7. Add deterministic adapter tests for successful mapping, expected protocol
   failure, malformed data, cancellation, and bounded retry behavior that the
   endpoint actually supports.
8. Run focused contract tests, typed-result verification, documentation
   validation, and root `check`.
9. Promote reusable client, mapping, security, or recovery decisions to their
   canonical ADR, standard, reference, guide, or runbook.

Return the consumer contract, adapter mapping, typed failures, security
boundary, test evidence, and verification results.
