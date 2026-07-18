---
title: Keep the Figma runtime boundary in TypeScript
type: adr
scope: repository
owner: figma-design-sync
status: accepted
last-reviewed: 2026-07-19
review-cycle-days: 365
sources:
  - repo/figma-design-sync/tools/src/figma
  - repo/figma-design-sync/tools/bin/build.mjs
  - repo/figma-design-sync/domain/src/main/kotlin/com/marmatsan/figmaDesignSync/domain/service/visual
  - repo/figma-design-sync/data/src/main/kotlin/com/marmatsan/figmaDesignSync/data/writer/OfficialMcpRunnerGenerator.kt
  - docs/decisions/adr-0002-distribute-figma-design-sync-as-gradle-plugin.md
---

# ADR-0003: Keep the Figma Runtime Boundary in TypeScript

## Context

Figma Design Sync now keeps repository modeling, deterministic visual planning,
writer configuration, official runner generation, MCP transport, execution
planning, and checkpoints in Kotlin. TypeScript remains inside `tools/src` for
the Figma Plugin API boundary, including component lookup, font loading,
measured layout, connector geometry, variable binding, and node mutation.

A Kotlin/JS spike tested whether moving measured geometry would improve this
boundary. Kotlin 2.4.0 successfully exported three geometry functions with
TypeScript declarations, esbuild bundled the generated output, and Node proved
behavioral parity. The production output comprised 6,764 bytes of JavaScript:
a 2,166-byte module, 4,032-byte Kotlin standard library, and 566-byte DOM
compatibility module. The declaration file was 487 bytes.

The first composite build and Node test took 85 seconds and executed 28 new or
changed tasks. An up-to-date production compilation took 3.2 seconds. Enabling
the Kotlin/JS Node test introduced a 25,998-byte, 551-line Yarn lock and Gradle-
managed Node, Yarn, and Mocha setup. Integrating the output into the portable
npm package would also require a generated-artifact handoff so installed
consumers do not need the Kotlin compiler.

The exported calculations still depend on measurements obtained from live
Figma nodes. Moving them does not remove the Figma API adapter or make the
runtime executable on the JVM; it introduces another JavaScript build and
publication boundary for a small reduction in TypeScript.

## Decision

Keep the executable Figma Plugin API boundary in TypeScript. Do not add
Kotlin/JS, Gradle-managed npm tooling, or generated Kotlin/JS artifacts to the
production build at this time.

Continue moving logic to Kotlin/JVM whenever it can be expressed as a pure plan
before entering Figma. TypeScript must consume those typed, language-neutral
plans and must not recreate repository modeling or deterministic visual rules.
Keep only behavior that requires the live Figma runtime in TypeScript, together
with thin boundary types and adapter tests.

Reconsider Kotlin/JS only when all of these conditions hold:

- a substantial body of reusable runtime logic can move without wrapping the
  Figma Plugin API itself;
- Gradle can produce one consumer-ready JavaScript artifact without requiring
  npm package consumers to run the Kotlin compiler;
- the generated artifact, lockfiles, scoped writer fingerprints, parity tests,
  and coordinated Maven/npm publication can be owned as one release contract.

## Consequences

- The source is Kotlin-first but intentionally not Kotlin-only.
- `.mcp.js` remains generated from a small TypeScript adapter and is still a
  build artifact, never a reviewed source of truth.
- Kotlin remains the owner of portable contracts and deterministic decisions;
  TypeScript remains the owner of Figma-specific effects and measured layout.
- The current npm package avoids a second generated-module handoff and
  Gradle-managed Node/Yarn toolchain.
- Future TypeScript reductions must remove duplication or move pure planning,
  not translate Figma API calls solely to change source language.

## Alternatives

- Replace the complete TypeScript writer with Kotlin/JS. Rejected because the
  Figma API still requires JavaScript interop declarations and runtime effects,
  while build and publication complexity increases.
- Move only the measured geometry helpers to Kotlin/JS now. Proven feasible by
  the spike but rejected because the artifact/toolchain handoff outweighs the
  small source reduction.
- Generate Figma mutation JavaScript as Kotlin strings. Rejected because this
  loses type checking at the Figma boundary and makes the generated source
  harder to test and maintain.
- Reimplement the Figma adapter on the JVM. Rejected because the injected MCP
  runtime exposes the Figma Plugin API inside JavaScript, not as a JVM API.

## Supersession

None.
