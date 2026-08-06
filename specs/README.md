# Active Specifications

`specs/` contains the approved intent for work that is currently being
implemented. It complements the current source of truth; it does not replace
executable code, tests, standards, or accepted ADRs.

Each active initiative uses `specs/<three-digit-id>-<kebab-case-name>/` with:

| File           | Purpose                                                                               |
|----------------|---------------------------------------------------------------------------------------|
| `spec.md`      | Outcome, required behavior, acceptance criteria, non-goals, and decisions.            |
| `plan.md`      | Ordered implementation slices, verification, and decision-documentation work.         |
| `checklist.md` | Completion evidence for implementation, architecture, testing, and documentation.     |
| `contracts/`   | Optional exact schemas or boundary contracts that genuinely need a separate artifact. |

Do not create empty research, quick-start, contract, or checklist artifacts.
Add a file only when it carries information required to execute or verify the
change.

## Precedence

An active specification defines desired change intent. It must remain
compatible with repository standards and accepted ADRs. When the requested
outcome conflicts with either, stop and record an explicit exception or new
decision before implementing the conflicting behavior.

Executable code and tests continue to describe current behavior until the
change is implemented. A specification must never be cited as proof that
unimplemented behavior already exists.

## Lifecycle

1. Create the package when the change needs durable context across more than
   one implementation step or contract.
2. Use `draft` while the outcome is still being shaped and `active` once its
   scope is approved.
3. Record decisions in `spec.md` as they are made.
4. Before completion, promote reusable knowledge to the appropriate standard,
   ADR, reference, guide, runbook, executable test, or schema.
5. Remove the active package in the final change after its durable knowledge
   has been promoted. Git history and the pull request retain the execution
   record without presenting an old plan as current policy.

Use the templates under `docs/templates/` and run
`.\gradlew.bat checkDocumentation` after changing a specification.
