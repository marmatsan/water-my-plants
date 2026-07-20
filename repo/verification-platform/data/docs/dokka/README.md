# Module verification-platform-data

Adapters for the provider-neutral repository CI domain.

This module reads Git and Gradle state, serializes CI reports, formats TeamCity
parameters and service messages, and calls the TeamCity queue API. It translates
external systems into domain contracts but does not decide verification policy.

# Package com.marmatsan.verificationPlatform.data.kotlin

KtLint-backed parsing and formatting for the standard rules plus
repository-owned Kotlin source rules. The adapter reads the root
`.editorconfig`, enforces vertical declaration and call layout for `.kt` and
`.kts` files, and delegates context-sensitive indentation to KtLint's standard
rule. In `.kt` files it also indexes same-file functions and constructors so
unambiguous positional calls can be rejected or rewritten with Kotlin parameter
names. Calls that require compiler type resolution remain a compiler and review
boundary.

# Package com.marmatsan.verificationPlatform.data.git

Git adapters that resolve committed repository changes.

# Package com.marmatsan.verificationPlatform.data.gradle

Gradle adapters that expose project modules and dependency edges to the domain.

# Package com.marmatsan.verificationPlatform.data.json

Serialization boundaries for provider-neutral plans and topology previews.

# Package com.marmatsan.verificationPlatform.data.teamcity

TeamCity parameter, service-message, and REST queue adapters.
