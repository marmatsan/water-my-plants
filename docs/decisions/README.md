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
