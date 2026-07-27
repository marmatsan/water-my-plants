package com.marmatsan.figmaDocumentationSync.domain.model.ci

import java.time.LocalDate

/**
 * Versioned snapshot of the Windows services that host the local CI runtime.
 *
 * @property schemaVersion Windows runtime contract schema version.
 * @property validation freshness metadata for the snapshot.
 * @property platform operating-system and host description.
 * @property services Windows services required by the CI runtime.
 */
data class CiWindowsRuntime(
    val schemaVersion: Int,
    val validation: Validation,
    val platform: String,
    val services: List<Service>,
) {
    init {
        require(schemaVersion > 0) { "CI Windows runtime schemaVersion must be positive" }
        require(validation.warnAfterDays > 0) { "CI Windows runtime warnAfterDays must be positive" }
        require(platform.isNotBlank()) { "CI Windows runtime platform must not be blank" }
        require(services.isNotEmpty()) { "CI Windows runtime must declare at least one service" }

        val serviceIds =
            services.map(
                transform = Service::id,
            )
        require(serviceIds.size == serviceIds.toSet().size) {
            "CI Windows runtime service ids must be unique"
        }
    }

    /**
     * Freshness policy for the Windows runtime snapshot.
     *
     * @property lastValidatedOn date the service topology was last verified.
     * @property warnAfterDays age after which CI emits a freshness warning.
     */
    data class Validation(
        val lastValidatedOn: LocalDate,
        val warnAfterDays: Int,
    )

    /**
     * One Windows service required by the local CI runtime.
     *
     * @property id stable service identity used by the visual model.
     * @property name human-readable service name.
     * @property description operational responsibility of the service.
     * @property service Windows service identifier.
     * @property startup expected startup mode.
     * @property identity operating-system identity that runs the service.
     */
    data class Service(
        val id: String,
        val name: String,
        val description: String,
        val service: String,
        val startup: String,
        val identity: String,
    ) {
        init {
            require(id.isNotBlank()) { "CI Windows runtime service id must not be blank" }
            require(name.isNotBlank()) { "CI Windows runtime service name must not be blank" }
            require(description.isNotBlank()) { "CI Windows runtime service description must not be blank" }
            require(service.isNotBlank()) { "CI Windows runtime service identifier must not be blank" }
            require(startup.isNotBlank()) { "CI Windows runtime startup must not be blank" }
            require(identity.isNotBlank()) { "CI Windows runtime identity must not be blank" }
        }
    }
}
