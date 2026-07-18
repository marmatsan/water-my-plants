---
title: Distribute Figma Design Sync as a Gradle plugin
type: adr
scope: repository
owner: figma-design-sync
status: accepted
last-reviewed: 2026-07-18
review-cycle-days: 365
sources:
  - repo/figma-design-sync/build.gradle.kts
  - repo/figma-design-sync/plugin/build.gradle.kts
  - repo/figma-design-sync/tools/package.json
  - repo/figma-design-sync/project-config/README.md
---

# ADR-0002: Distribute Figma Design Sync as a Gradle Plugin

## Context

Figma Design Sync is internally separated into domain, data, Gradle plugin,
TypeScript writer, optional CI adapter, and Water My Plants project
configuration. Consuming the source as an included build preserves those
boundaries but forces every repository to know the implementation layout.

The distribution must keep the Kotlin-first architecture, avoid copying
PowerShell orchestration, keep repository identities outside the engine, and
allow CI systems other than TeamCity.

## Decision

Publish `com.marmatsan.figmaDesignSync` as the single public Gradle entry point.
Keep domain and data as transitive Maven implementation artifacts rather than
flattening the source modules. Publish `catalog-core` as their reusable
supporting API and publish `teamcity-adapter` separately as an optional
artifact.

Publish the portable TypeScript writer as
`@marmatsan/figma-design-sync-tools`. Its build executable accepts a
repository-owned `figma-config.ts` and materializes a configured workspace.
The Water My Plants `project-config` remains source owned by this repository
and is not part of the portable release.

Use one version for the Maven artifacts, Gradle plugin marker, supporting
catalog API, and TypeScript package. Prove each staged release from a standalone
consumer that has no included-build dependency on the source tree.

No external package repository is selected by this ADR. The build supports a
workspace staging repository and a configurable Maven URL; credentials,
signing, registry ownership, and the first public version require explicit
release authorization.

## Consequences

- Consumers see one Gradle plugin instead of the implementation modules.
- Internal Clean Architecture boundaries and focused tests remain intact.
- TeamCity remains optional and does not enter the portable plugin classpath.
- A release contains coordinated Maven and npm artifacts.
- The release process must publish transitive artifacts in dependency order.
- Project adapters remain small repository-owned Gradle plugins and TypeScript
  configuration modules.
- A separate decision may select Maven Central, the Gradle Plugin Portal,
  GitHub Packages, or another registry and its signing policy.

## Alternatives

- Publish one shaded JAR. Rejected because it hides dependency boundaries,
  complicates Gradle plugin metadata, and would bundle the optional TeamCity
  adapter or require additional shading variants.
- Merge all Kotlin modules. Rejected because distribution convenience should
  not remove the dependency rules already enforced by the architecture.
- Continue requiring `includeBuild`. Retained for repository development but
  rejected as the public consumption contract.
- Publish `project-config` as part of the engine. Rejected because it would
  reintroduce Water My Plants paths, Figma ids, catalogs, and CI assumptions.

## Supersession

None.
