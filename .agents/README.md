# Repository Agent Adapters

This directory contains concise repository-scoped procedures for AI-assisted
work. It does not own engineering policy.

Before using a reviewer profile or skill, apply the root
[`AGENTS.md`](../AGENTS.md), the
[code-generation standard](../docs/standards/code-generation.md), the
[code-generation context map](../docs/reference/code-generation-context.md),
and any active [specification](../specs/README.md).

## Reviewer Profiles

| Profile | Review outcome |
|---------|----------------|
| [`reviewers/architecture-solid.md`](reviewers/architecture-solid.md) | Verify dependency direction, package cohesion, public boundaries, and all five SOLID principles. |
| [`reviewers/testing.md`](reviewers/testing.md) | Verify behavior ownership, regression evidence, reliability, and living documentation. |
| [`reviewers/documentation.md`](reviewers/documentation.md) | Verify source precedence, document type, decision promotion, links, indexes, and coverage. |

Profiles are review lenses. They report evidence and actionable findings; they
do not silently rewrite the implementation or create new policy.

## Skills

| Skill | Supported outcome |
|-------|-------------------|
| `add-product-feature` | Add one observable product capability behind explicit boundaries. |
| `add-compose-screen` | Add one state-driven, accessible Compose screen. |
| `add-product-module` | Add one focused product/core Gradle module. |
| `add-api-adapter` | Add one transport adapter without leaking transport types inward. |

Each skill selects an existing guide and its standards, inspects current
executable examples, implements the narrow outcome, and runs focused checks.
Detailed patterns remain in canonical repository documents linked by the
skill.

Add another profile or skill only after a repeated workflow demonstrates a
stable trigger, inputs, outcome, and verification contract.

## Validation

`checkDocumentation` validates each repository `SKILL.md` name, directory
identity, description, supported frontmatter fields, and local Markdown links.
Run it for every skill change.

Also run the installed `skill-creator` `quick_validate.py` against each changed
skill before review. That compatibility validator requires Python with
`PyYAML`; a missing `yaml` module means the validator environment is incomplete,
not that the skill contract failed.
