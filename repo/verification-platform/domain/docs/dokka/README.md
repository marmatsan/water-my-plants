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

# Package com.marmatsan.verificationPlatform.domain.model.ci

Defines provider-neutral CI plans, scopes, verification units, and topologies.

# Package com.marmatsan.verificationPlatform.domain.model.documentation

Defines documentation validation outcomes.

# Package com.marmatsan.verificationPlatform.domain.model.git

Defines branch validation and repository change-set contracts.

# Package com.marmatsan.verificationPlatform.domain.model.modules

Defines repository modules, dependency graphs, and change impacts.

# Package com.marmatsan.verificationPlatform.domain.model.teamcity

Defines provider-facing TeamCity run requests and queued-run identities.

# Package com.marmatsan.verificationPlatform.domain.port.teamcity

Defines the consumer-owned boundary implemented by TeamCity run adapters.

# Package com.marmatsan.verificationPlatform.domain.service.ci

Classifies changes into verification plans and optional execution topologies.

# Package com.marmatsan.verificationPlatform.domain.service.documentation

Validates documentation coverage without filesystem dependencies.

# Package com.marmatsan.verificationPlatform.domain.service.git

Validates branch names against the repository workflow contract.

# Package com.marmatsan.verificationPlatform.domain.service.modules

Computes direct and transitive module impact from repository changes.

# Package com.marmatsan.verificationPlatform.domain.service.teamcity

Queues provider operations through the domain-owned TeamCity port.
