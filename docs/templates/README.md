# Documentation Templates

Copy the template matching the document type into its canonical directory,
replace all placeholder metadata, and add the result to the closest README
index.

| Template                 | Destination                                     |
|--------------------------|-------------------------------------------------|
| `standard.md`            | `docs/standards/` or `<module>/docs/standards/` |
| `guide.md`               | `docs/guides/` or `<module>/docs/guides/`       |
| `runbook.md`             | `docs/runbooks/` or `<module>/docs/runbooks/`   |
| `reference.md`           | `docs/reference/` or `<module>/docs/reference/` |
| `adr.md`                 | `docs/decisions/adr-NNNN-*.md`                  |
| `module-readme.md`       | `<module>/docs/README.md`                       |
| `specification.md`       | `specs/NNN-kebab-case-name/spec.md`             |
| `implementation-plan.md` | `specs/NNN-kebab-case-name/plan.md`             |
| `checklist.md`           | `specs/NNN-kebab-case-name/checklist.md`        |

Templates are excluded from metadata validation because they intentionally
contain placeholders. Every document created from them is validated by CI.
