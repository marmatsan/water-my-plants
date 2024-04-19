package com.marmatsan.dev.catalog_domain.usecase.plant_screen

import com.marmatsan.dev.catalog_domain.model.PlantDataConstraints
import com.marmatsan.dev.core_domain.usecase.UseCase

class ValidatePlantNameUseCase : UseCase {
    operator fun invoke(
        plantName: String
    ): Pair<Boolean, String?> {
        val isValid = plantName.length <= PlantDataConstraints.PLANT_NAME_MAX_LENGTH
        return Pair(isValid, plantName)
    }
}