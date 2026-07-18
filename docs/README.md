# Project Documentation

Use [documentation.md](documentation.md) for the canonical documentation
standard and source-of-truth hierarchy.

| Area | Purpose |
|------|---------|
| `standards/` | Mandatory and recommended engineering rules. |
| `guides/` | Supported development workflows. |
| `reference/` | Exact project contracts and inventories. |
| `decisions/` | Architecture Decision Records. |
| `runbooks/` | Operational execution and recovery procedures. |
| `ci/` | Versioned CI topology, branch protection, and validation contracts. |
| `uml/` | Project-wide PlantUML sources and shared includes. |
| `templates/` | Starting points for typed documentation. |

Module-specific documentation belongs in the module's top-level `docs/`
directory and links back to shared project standards instead of duplicating
them.

## Architecture Decisions

- [ADR-0001: Adopt a Typed Documentation System](decisions/adr-0001-documentation-system.md)
- [ADR-0002: Distribute Figma Design Sync as a Gradle Plugin](decisions/adr-0002-distribute-figma-design-sync-as-gradle-plugin.md)
- [ADR-0003: Keep the Figma Runtime Boundary in TypeScript](decisions/adr-0003-keep-figma-runtime-boundary-in-typescript.md)
