package com.marmatsan.figmaDesignSync.domain.model.ci

import java.time.LocalDate

/**
 * Versioned snapshot of the Windows services that host the local CI runtime.
 */
data class CiWindowsRuntime(
    val schemaVersion: Int,
    val validation: Validation,
    val platform: String,
    val services: List<Service>
) {
    init {
        require(schemaVersion > 0) { "CI Windows runtime schemaVersion must be positive" }
        require(validation.warnAfterDays > 0) { "CI Windows runtime warnAfterDays must be positive" }
        require(platform.isNotBlank()) { "CI Windows runtime platform must not be blank" }
        require(services.isNotEmpty()) { "CI Windows runtime must declare at least one service" }

        val serviceIds = services.map(Service::id)
        require(serviceIds.size == serviceIds.toSet().size) {
            "CI Windows runtime service ids must be unique"
        }
    }

    data class Validation(
        val lastValidatedOn: LocalDate,
        val warnAfterDays: Int
    )

    data class Service(
        val id: String,
        val name: String,
        val description: String,
        val service: String,
        val startup: String,
        val identity: String
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
