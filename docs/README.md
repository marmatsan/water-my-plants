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
- [ADR-0007: Use Trunk-Based Development](decisions/adr-0007-use-trunk-based-development.md)
- [ADR-0005: Name the Portable Infrastructure Figma Documentation Sync](decisions/adr-0005-name-figma-documentation-sync.md)
- [ADR-0003: Keep the Figma Runtime Boundary in TypeScript](decisions/adr-0003-keep-figma-runtime-boundary-in-typescript.md)
- [ADR-0004: Use JSON for Figma Writer Project Configuration](decisions/adr-0004-use-json-for-figma-writer-project-configuration.md)

## CI References

- [CI Verification Plan](reference/ci-verification-plan.md)

## Git Workflow

- [Git Workflow Standard](standards/git-workflow.md)
- [Work With Short-Lived Git Branches](guides/work-with-short-lived-branches.md)
- [Create A Release](runbooks/create-release.md)
- [Ship A Production Hotfix](runbooks/ship-hotfix.md)
- [Main Branch Protection](ci/main-branch-protection.md)
