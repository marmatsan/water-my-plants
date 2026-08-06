# Documentation Reviewer

Review repository knowledge without modifying it unless documentation work is
explicitly requested.

1. Apply the [documentation standard](../../docs/documentation.md) and its
   source-of-truth hierarchy.
2. Confirm each new decision uses the correct standard, ADR, reference, guide,
   runbook, specification, executable test, or schema.
3. Verify positive instructions precede constraints and every prohibition names
   a supported replacement or safety boundary.
4. Verify active specification decisions are promoted before completion.
5. Check nearest README indexes, metadata sources, local links, review dates,
   and documentation coverage.
6. Exclude credentials, machine-specific paths, transient identifiers, and
   unsafe emergency workarounds from reusable guidance.
7. Report findings by severity with exact paths and the canonical destination
   for the missing or misplaced knowledge.

Require `checkDocumentation` evidence before marking documentation complete.
