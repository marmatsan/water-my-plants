---
name: add-product-module
description: Create one focused Water My Plants product or core Gradle module with a capability name, permitted dependencies, convention plugins, module documentation, and verification. Use only when an approved feature or shared capability demonstrates a durable module boundary.
---

# Add Product Module

1. Read the root and scoped `AGENTS.md` files and the active specification.
2. Read the
   [module guide](../../../docs/guides/add-module.md), the
   [architecture standard](../../../docs/standards/architecture.md), the
   [Gradle standard](../../../docs/standards/gradle.md), and the
   [project structure reference](../../../docs/reference/project-structure.md).
3. Identify the capability owner, consumers, public boundary, and dependency
   direction. Confirm an existing module cannot own the behavior coherently.
4. Choose a capability-based module path and register it in the existing
   settings composition with the required convention plugins and namespace.
5. Declare only permitted dependencies through type-safe project accessors and
   generated catalogs.
6. Create the module README from the repository template and record purpose,
   boundaries, consumers, dependencies, shared standards, and focused checks.
7. Add source sets only for real behavior and drive them with focused tests.
8. Update the project structure reference and any executable module graph or
   documentation coverage contract affected by the new boundary.
9. Run the module `check`, module-boundary checks, documentation validation,
   and root `check`.

Return the new boundary, dependency arrows, public consumers, documentation,
and verification evidence.
