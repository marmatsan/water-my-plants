---
title: Publish a Figma Documentation Sync release
type: runbook
scope: repo/figma-documentation-sync
owner: figma-documentation-sync
status: active
last-reviewed: 2026-07-19
review-cycle-days: 90
sources:
  - repo/figma-documentation-sync/gradle.properties
  - repo/figma-documentation-sync/build.gradle.kts
  - repo/figma-documentation-sync/tools/package.json
  - repo/figma-documentation-sync/samples/standalone-consumer
---

# Publish a Figma Documentation Sync Release

## Purpose

Use this runbook only after the team chooses the Maven and npm registries and
explicitly authorizes a release. It prepares and verifies the complete artifact
set without changing the Figma official model.

## Prerequisites

- Start from a clean, tested `main` revision and create a short-lived release
  preparation branch.
- Choose one semantic version and the external Maven and npm destinations.
- Confirm ownership of the Maven group, Gradle plugin id, npm scope, and any
  required signing identity.
- Store repository credentials in the CI secret store. Reference them through
  provider-specific environment variables or Gradle credentials; never write
  them into build files, `gradle.properties`, `.npmrc`, or documentation.
- Keep `tools/package.json` private until the release PR explicitly approves
  npm publication.

## Procedure

1. Replace `0.1.0-SNAPSHOT` in
   `repo/figma-documentation-sync/gradle.properties` and `tools/package.json` with the
   same release version. Update the lockfile with:

   ```powershell
   Push-Location repo\figma-documentation-sync\tools
   npm install --package-lock-only --ignore-scripts
   Pop-Location
   ```

2. Build all Maven artifacts into workspace staging and resolve the plugin
   from the standalone consumer:

   ```powershell
   .\gradlew.bat :figma-documentation-sync:verifyStagedPublication --stacktrace
   ```

3. Run the Kotlin and TypeScript checks:

   ```powershell
   .\gradlew.bat :figma-documentation-sync:domain:check `
       :figma-documentation-sync:data:check `
       :figma-documentation-sync:teamcity-adapter:check `
       :figma-documentation-sync:plugin:check `
       :figma-documentation-sync:project-config:check

   .\gradlew.bat testFigmaDocumentationSyncTools buildFigmaDocumentationSyncTools

   Push-Location repo\figma-documentation-sync\tools
   $env:npm_config_cache = "..\..\..\build\npm-cache"
   npm pack --dry-run
   Remove-Item Env:npm_config_cache
   Pop-Location
   ```

4. Inspect the staged POMs, Gradle module metadata, plugin marker, sources
   JARs, npm file inventory, and checksums. Confirm that `project-config` and
   Water My Plants credentials are absent.

5. Configure the selected Maven repository URL through
   `-PfigmaDocumentationSyncPublicationRepository=<repository-url>`. Add authentication
   and signing only through the chosen provider's supported secret mechanism.

6. In the release PR, remove the npm `private` guard only after the npm scope
   and destination have been approved. Publish the npm package with the same
   version as Maven.

7. Publish in dependency order: `catalog-core`, domain, data, the Gradle plugin
   implementation and marker, then the optional TeamCity adapter. The Gradle
   aggregate task preserves this dependency set even when the repository
   executes independent upload tasks.

8. Verify from a clean external consumer, merge the release PR, create the
   `v<version>` Git tag, and then restore the next development version in a
   separate change if continuing development.

## Verification

A release is successful only when:

- the plugin resolves through `plugins { id("com.marmatsan.figmaDocumentationSync") }`;
- the consumer does not use `includeBuild("repo/figma-documentation-sync")`;
- the Gradle tasks and `figmaDocumentationSync` extension are registered;
- the TypeScript package materializes a writer with the consumer's config;
- all published artifacts report the same version;
- no Water My Plants `project-config` artifact is present.

## Recovery

- If staging fails, fix the source and rerun the staging task; workspace Maven
  files are generated build artifacts.
- If an external immutable version was uploaded partially, do not overwrite
  it. Mark it unusable according to registry policy and release a new patch
  version.
- If npm was published but Maven failed, deprecate the npm version until the
  matching Maven release exists.
- If credentials appear in output, revoke them immediately and remove the
  affected logs or artifacts according to the registry incident process.

## Prohibited Actions

- Do not publish from a feature branch, dirty checkout, or unverified staging
  repository.
- Do not publish `project-config` or `water-my-plants-catalog` as portable
  artifacts.
- Do not use different Maven and npm versions.
- Do not bypass `verifyStagedPublication` or make `includeBuild` part of the
  consumer fixture.
- Do not write official Figma metadata as part of a software package release.

## Sources

- [Distribution contract](../reference/distribution-contract.md)
- [Adoption guide](../guides/adopting-figma-documentation-sync.md)
- [ADR-0005](../../../../docs/decisions/adr-0005-name-figma-documentation-sync.md)
- [`../../samples/standalone-consumer`](../../samples/standalone-consumer)
