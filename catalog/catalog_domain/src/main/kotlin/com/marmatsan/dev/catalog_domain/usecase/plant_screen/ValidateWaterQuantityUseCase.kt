package com.marmatsan.dev.catalog_domain.usecase.plant_screen

import com.marmatsan.dev.core_domain.catalog.PlantDataConstraints
import com.marmatsan.dev.core_domain.hasAtMostLengthOf
import com.marmatsan.dev.core_domain.usecase.UseCase

class ValidateWaterQuantityUseCase : UseCase {
    operator fun invoke(
        waterAmount: String
    ): Pair<Boolean, Int?> {
        val isValid =
            waterAmount hasAtMostLengthOf PlantDataConstraints.WATER_AMOUNT_MAX_LENGTH
        val intWaterAmount = waterAmount.toIntOrNull()
        return Pair(isValid, intWaterAmount)
    }
}