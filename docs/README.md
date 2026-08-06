# Project Documentation

Use [documentation.md](documentation.md) for the canonical documentation
standard and source-of-truth hierarchy.

| Area                              | Purpose                                                             |
|-----------------------------------|---------------------------------------------------------------------|
| `standards/`                      | Mandatory and recommended engineering rules.                        |
| `guides/`                         | Supported development workflows.                                    |
| `reference/`                      | Exact project contracts and inventories.                            |
| `decisions/`                      | Architecture Decision Records.                                      |
| `runbooks/`                       | Operational execution and recovery procedures.                      |
| `ci/`                             | Versioned CI topology, branch protection, and validation contracts. |
| `uml/`                            | Project-wide PlantUML sources and shared includes.                  |
| `templates/`                      | Starting points for typed documentation.                            |
| [`../specs/`](../specs/README.md) | Approved active change intent, plans, and completion evidence.      |

Module-specific documentation belongs in the module's top-level `docs/`
directory and links back to shared project standards instead of duplicating
them.

## Architecture Decisions

- [ADR-0001: Adopt a Typed Documentation System](decisions/adr-0001-documentation-system.md)
- [ADR-0007: Use Trunk-Based Development](decisions/adr-0007-use-trunk-based-development.md)
- [ADR-0005: Name the Portable Infrastructure Figma Documentation Sync](decisions/adr-0005-name-figma-documentation-sync.md)
- [ADR-0003: Keep the Figma Runtime Boundary in TypeScript](decisions/adr-0003-keep-figma-runtime-boundary-in-typescript.md)
- [ADR-0004: Use JSON for Figma Writer Project Configuration](decisions/adr-0004-use-json-for-figma-writer-project-configuration.md)
- [ADR-0009: Separate the Product Catalog From Build-Tool Versions](decisions/adr-0009-separate-product-catalog-from-build-tool-versions.md)
- [ADR-0010: Isolate Product Composition From Reusable Builds](decisions/adr-0010-isolate-product-composition-from-reusable-builds.md)
- [ADR-0011: Standardize Typed Errors With kotlin-result](decisions/adr-0011-standardize-typed-errors-with-kotlin-result.md)
- [ADR-0017: Use OOUX And BDD Before Product Implementation](decisions/adr-0017-use-ooux-and-bdd-before-product-implementation.md)

## Engineering Standards

- [Code-Generation Standard](standards/code-generation.md)
- [Product Design Standard](standards/product-design.md)
- [Product Architecture Standard](standards/architecture.md)
- [Gradle Build Standard](standards/gradle.md)
- [Repository Hygiene Standard](standards/repository-hygiene.md)
- [Typed Error Handling Standard](standards/error-handling.md)
- [UML Documentation Standard](standards/uml.md)

## Code-Generation Reference

- [Code-Generation Context Map](reference/code-generation-context.md)
- [Product Design Workspace](reference/product-design-workspace.md)
- [Design A Product Feature](guides/design-product-feature.md)
- [Active Specifications](../specs/README.md)

## CI References

- [CI Verification Plan](reference/ci-verification-plan.md)

## Git Workflow

- [Git Workflow Standard](standards/git-workflow.md)
- [Work With Short-Lived Git Branches](guides/work-with-short-lived-branches.md)
- [Create A Release](runbooks/create-release.md)
- [Ship A Production Hotfix](runbooks/ship-hotfix.md)
- [Main Branch Protection](ci/main-branch-protection.md)
- [Bootstrap The TeamCity CI Gate](runbooks/bootstrap-teamcity-ci-gate.md)
