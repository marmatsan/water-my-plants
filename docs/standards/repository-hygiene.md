---
title: Repository hygiene standard
type: standard
scope: repository
owner: repository-tooling
status: active
last-reviewed: 2026-08-06
review-cycle-days: 180
sources:
  - .gitignore
  - docs/reference/project-structure.md
  - repo/verification-platform/plugin/src/main/kotlin/com/marmatsan/verificationPlatform/plugin/task/git/CheckRepositoryDiffTask.kt
---

# Repository Hygiene Standard

## Purpose

Define ownership, lifecycle, and safe removal for generated output, temporary
artifacts, and retired modules.

## Temporary Artifact Ownership

- Code that creates a temporary file or directory MUST own its lifecycle and
  remove it after its final consumer finishes.
- A producer cleans partially written temporary artifacts when it fails.
- An artifact that intentionally crosses process or operational phases MAY
  survive its producer, but its final consumer or documented completion step
  deletes it.
- Repository-local temporary artifacts live below `tmp/`, a module `build/`
  directory, or another explicitly ignored generated-output root.
- Tests register temporary resources with a lifecycle-aware fixture or delete
  them from `finally`.
- Treat generated files as build or publication artifacts. Keep the reviewed
  source and a reproducible generation path instead of committing output unless
  a contract explicitly requires the generated file.

## Module Retirement

A migration that replaces, renames, merges, or removes a module is complete
only after the retired module path is absent from the checkout.

1. Remove the retired module from Gradle composition, project dependencies,
   catalogs, plugin registrations, CI configuration, and current
   documentation.
2. Preserve accepted ADR references when they remain historical evidence;
   those references do not keep the retired path alive.
3. Resolve and verify the exact absolute module path inside the repository.
4. Inspect tracked, untracked, and ignored contents before deletion so
   user-owned work is not removed accidentally.
5. Remove tracked source and ignored build state owned by the retired module,
   including `.gradle/`, `.kotlin/`, and `build/` directories.
6. Verify the former path is absent, repository status contains only the
   intended migration, and the replacement passes focused and boundary checks.

The migration owner performs retirement cleanup because a removed build no
longer contributes its own `clean` tasks to the active graph.

## Disallowed Alternatives

| Do not use | Use instead | Reason |
|------------|-------------|--------|
| A broad recursive delete inferred from an unknown directory | An explicitly resolved and inspected target path | Protects user-owned and unrelated repository data. |
| Successful assertions as temporary-resource cleanup | A lifecycle fixture or `finally` cleanup | Preserves cleanup on failure. |
| A generated output as reviewed source | Versioned input plus regeneration instructions | Keeps authorship and review reproducible. |
| A retired empty directory or ignored cache left behind | Verified complete path removal | Prevents stale boundaries and future ambiguity. |

## Exceptions

An artifact may remain after its producer only when a documented consumer owns
the next phase and cleanup. A checked-in generated artifact requires a source
contract that identifies why review or runtime cannot consume the original
source directly.

## Verification

- Run `./gradlew cleanTemporaryArtifacts` after supervised repository work that
  creates root-level temporary or generated tooling artifacts.
- Inspect `git status --short` for unintended tracked or untracked output.
- A module retirement additionally verifies that the former directory is
  absent and runs the replacement build's focused checks, module boundaries,
  documentation validation, and root `check`.

`cleanTemporaryArtifacts` removes outputs owned by the active repository graph.
It MUST NOT infer and recursively delete unknown directories below `repo/`.

## Sources

- `.gitignore`
- [Project structure](../reference/project-structure.md)
- `repo/verification-platform/plugin/src/main/kotlin/com/marmatsan/verificationPlatform/plugin/task/git/CheckRepositoryDiffTask.kt`
