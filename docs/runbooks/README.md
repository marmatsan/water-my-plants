# Project Runbooks

Runbooks execute or recover repository-wide operations. Module-owned operations
remain under that module's `docs/runbooks/` directory.

| Runbook                                                         | Operation                                                                                             |
|-----------------------------------------------------------------|-------------------------------------------------------------------------------------------------------|
| [Bootstrap The TeamCity CI Gate](bootstrap-teamcity-ci-gate.md) | Introduce or recover the required GitHub status publisher without leaving `main` unprotected.         |
| [TeamCity Cloudflare Access](teamcity-cloudflare-access.md)     | Operate TeamCity HTTPS access, service authentication, webhooks, and Windows runtime recovery.        |
| [TeamCity Backup And Recovery](teamcity-backup-recovery.md)     | Back up TeamCity server state and artifacts, verify off-host copies, and run isolated restore drills. |
| [Create A Release](create-release.md)                           | Tag a verified `main` commit and run exceptional release stabilization.                               |
| [Ship A Production Hotfix](ship-hotfix.md)                      | Deliver an urgent correction through the protected trunk and publish a patch tag.                     |
