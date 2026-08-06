---
name: add-product-feature
description: Add or materially extend one Water My Plants product capability with explicit module boundaries, domain behavior, adapters, UI state, tests, and documentation. Use when implementing a user-visible feature or business rule under app, core, or a feature module; do not use for repository tooling or an isolated operational change.
---

# Add Product Feature

1. Read the root `AGENTS.md`, the active specification, and every scoped
   `AGENTS.md` for the target paths.
2. Read the
   [feature guide](../../../docs/guides/add-feature.md), the
   [code-generation standard](../../../docs/standards/code-generation.md), and
   every standard selected by the
   [context map](../../../docs/reference/code-generation-context.md).
3. Inspect the owning module README, current source, tests, and public
   consumers. Treat explicitly identified scaffold code as non-authoritative.
4. Clarify the observable behavior with concrete examples. Add or update a
   `.feature` before production code only when it adds durable business value.
5. Decide whether the capability belongs in an existing module. Invoke the
   `add-product-module` skill only when the feature demonstrates a new durable
   module boundary.
6. Implement the domain behavior, consumer-owned boundaries, adapters,
   composition, and UI exposure in the order required by the guide.
7. Drive each implementation step with the narrowest deterministic test and
   preserve the acceptance criteria from the active specification.
8. Run focused module checks, boundary checks when dependencies change,
   `checkDocumentation` when knowledge changes, and root `check` before
   completion.
9. Review SRP, OCP, LSP, ISP, and DIP and promote reusable decisions or
   verification learning to their canonical documents.

Return the implemented outcome, changed boundaries, verification evidence, and
any decision that still requires maintainer approval.
