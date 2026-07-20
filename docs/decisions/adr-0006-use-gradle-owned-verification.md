---
title: Use Gradle-owned repository verification
type: adr
scope: repository
owner: repository-tooling
status: accepted
last-reviewed: 2026-07-20
review-cycle-days: 365
sources:
  - repo/ci/domain/src/main/kotlin/com/marmatsan/ci/domain/service/CiPlanFactory.kt
  - repo/ci/plugin/src/main/kotlin/com/marmatsan/ci/plugin/CiGradlePlugin.kt
  - .teamcity/settings.kts
---

# ADR-0006: Use Gradle-Owned Repository Verification

## Context

Repository verification was split between Gradle tasks, direct TeamCity
commands, Maven, Git, and a PowerShell documentation validator. Although change
classification was already Kotlin-owned, local and CI entry points did not
express the same verification contract. This increased provider coupling and
made a future multi-agent topology repeat execution policy.

The project is Kotlin-first, uses Gradle as its build system, currently has one
Windows agent, and must preserve one authoritative `TeamCity CI` GitHub status.
Agent discovery, credentials, artifacts, and status publication remain provider
responsibilities.

## Decision

Gradle tasks are the canonical repository verification API. The provider-neutral
Kotlin plan maps required units to allow-listed task names and exports their
ordered, de-duplicated union as `ci.plan.gradleTasks`.

TeamCity performs agent preflight, invokes `prepareTeamCityCiPlan`, runs the
planned Gradle tasks, and publishes evidence. Documentation validation and Git
diff verification are implemented in Kotlin. TeamCity DSL generation remains a
Maven operation behind the provider-specific `checkTeamCityDsl` Gradle task.

The root `check` lifecycle remains exhaustive and fail-closed. Targeted CI plans
may select smaller module checks only when the Kotlin classifier proves that the
change is safely scoped.

## Consequences

- Local and TeamCity verification use the same named Gradle entry points.
- PowerShell no longer owns repository validation policy.
- Gradle caching, task dependencies, reports, and future agent partitioning can
  be reused without duplicating rules in a CI provider.
- TeamCity remains necessary for checkout, capabilities, credentials,
  scheduling, artifacts, and the GitHub status.
- Provider-specific tools such as Maven may still execute behind narrow Gradle
  adapters.
- Applying the root build is required even for documentation-only verification,
  so configuration cost remains visible and should be measured.

## Alternatives

- Keep separate TeamCity script steps. Rejected because local and CI contracts
  would continue to diverge and PowerShell would retain policy ownership.
- Put every check inside one monolithic Gradle task action. Rejected because it
  would hide task dependencies, caching, reports, and focused local execution.
- Move verification policy to GitHub Actions. Rejected because it changes the CI
  provider without solving portability and duplicates the current TeamCity
  investment.

## Supersession

None.
