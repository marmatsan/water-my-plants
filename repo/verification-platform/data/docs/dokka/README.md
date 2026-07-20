# Module verification-platform-data

Adapters for the provider-neutral repository CI domain.

This module reads Git and Gradle state, serializes CI reports, formats TeamCity
parameters and service messages, and calls the TeamCity queue API. It translates
external systems into domain contracts but does not decide verification policy.

# Package com.marmatsan.verificationPlatform.data.git

Git adapters that resolve committed repository changes.

# Package com.marmatsan.verificationPlatform.data.gradle

Gradle adapters that expose project modules and dependency edges to the domain.

# Package com.marmatsan.verificationPlatform.data.json

Serialization boundaries for provider-neutral plans and topology previews.

# Package com.marmatsan.verificationPlatform.data.teamcity

TeamCity parameter, service-message, and REST queue adapters.
