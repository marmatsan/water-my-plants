package com.marmatsan.dev.catalog_domain.usecase.plant_screen

import com.marmatsan.dev.catalog_domain.model.PlantDataConstraints
import com.marmatsan.dev.core_domain.usecase.NonSuspendingUseCase

class ValidatePlantNameUseCase : NonSuspendingUseCase<String, Pair<Boolean, String?>> {
    override operator fun invoke(
        input: String
    ): Pair<Boolean, String?> {
        val isValid = input.length <= PlantDataConstraints.PLANT_NAME_MAX_LENGTH
        return Pair(isValid, input)
    }
}