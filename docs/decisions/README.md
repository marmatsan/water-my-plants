# Architecture Decision Records

ADRs explain durable project decisions and their tradeoffs. Use
[`../templates/adr.md`](../templates/adr.md) and name records
`adr-NNNN-kebab-case-title.md`.

| ADR | Status | Decision |
|-----|--------|----------|
| [ADR-0001](adr-0001-documentation-system.md) | Accepted | Use typed docs-as-code with CI validation. |
| [ADR-0002](adr-0002-distribute-figma-design-sync-as-gradle-plugin.md) | Superseded | Expose one versioned Gradle plugin while retaining internal modules and optional adapters. |
| [ADR-0003](adr-0003-keep-figma-runtime-boundary-in-typescript.md) | Superseded | Keep only the live Figma Plugin API boundary in TypeScript. |
| [ADR-0004](adr-0004-use-json-for-figma-writer-project-configuration.md) | Superseded | Use Kotlin-generated JSON as the only writer project-configuration input. |
| [ADR-0005](adr-0005-name-figma-documentation-sync.md) | Accepted | Publish the retained architecture as Figma Documentation Sync. |
| [ADR-0006](adr-0006-use-gradle-owned-verification.md) | Accepted | Use Gradle tasks as the canonical repository verification API. |
| [ADR-0007](adr-0007-use-trunk-based-development.md) | Accepted | Use one protected trunk with short-lived branches and squash integration. |
| [ADR-0008](adr-0008-use-canonical-for-authoritative-figma-sync.md) | Accepted | Use canonical for the authoritative Figma Sync process and reserve official for vendor-provided technology. |
| [ADR-0009](adr-0009-separate-product-catalog-from-build-tool-versions.md) | Accepted | Keep build-tool versions local and exclude included-build dependency catalogs from the product tree. |
| [ADR-0010](adr-0010-isolate-product-composition-from-reusable-builds.md) | Accepted | Keep product wiring in one composition build and allow reusable builds to consume only public APIs. |
| [ADR-0011](adr-0011-standardize-typed-errors-with-kotlin-result.md) | Accepted | Use kotlin-result for typed recoverable failures while capabilities own their error hierarchies. |
| [ADR-0012](adr-0012-coordinate-repository-gradle-plugin-releases.md) | Accepted | Release repository convention plugins together and derive accurate plugin usage in Figma. |
