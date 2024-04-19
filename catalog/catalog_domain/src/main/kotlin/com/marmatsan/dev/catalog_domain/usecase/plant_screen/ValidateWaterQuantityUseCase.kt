package com.marmatsan.dev.catalog_domain.usecase.plant_screen

import com.marmatsan.dev.catalog_domain.model.PlantDataConstraints
import com.marmatsan.dev.core_domain.usecase.NonSuspendingUseCase

class ValidateWaterQuantityUseCase : NonSuspendingUseCase<String, Pair<Boolean, Int?>> {
    override fun invoke(
        input: String
    ): Pair<Boolean, Int?> {
        val isValid = input.length <= PlantDataConstraints.WATER_AMOUNT_MAX_LENGTH
        val intWaterAmount = input.toIntOrNull()
        return Pair(isValid, intWaterAmount)
    }
}