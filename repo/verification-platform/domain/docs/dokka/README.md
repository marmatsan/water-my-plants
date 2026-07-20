# Module verification-platform-domain

Provider-neutral models and services for repository verification planning.

This module decides which verification units apply to a committed change and
how those units could be assigned to one or more build agents. It has no Gradle,
TeamCity, Git, filesystem, or HTTP dependencies.

Read this module from its behavior entry points:

1. `CiPlanFactory` classifies a `RepositoryChangeSet` against a
   `RepositoryModuleGraph` and creates a `CiPlan`.
2. `CiTopologyPlanner` projects the required verification units into a
   `CiExecutionTopology` without changing the plan's authority.
3. `ModuleImpactAnalyzer` resolves changed modules and transitive reverse
   dependents.

The executable behavior lives in `repo/verification-platform/domain/src/test/resources/`. Dokka
documents the Kotlin API; it does not replace those scenarios.

# Package com.marmatsan.verificationPlatform.domain.model

Serializable provider-neutral contracts shared by CI adapters.

# Package com.marmatsan.verificationPlatform.domain.service

Pure services that classify changes, compute module impact, plan agent lanes,
and queue provider operations through domain ports.

# Package com.marmatsan.verificationPlatform.domain.port

Boundaries implemented by provider-specific adapters outside the domain.
