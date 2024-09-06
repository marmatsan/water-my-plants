package com.marmatsan.dev.catalog_domain.usecase.plant_screen

import com.marmatsan.dev.catalog_domain.model.PlantDataConstraints
import com.marmatsan.dev.core_domain.result.Error
import com.marmatsan.dev.core_domain.result.Result
import com.marmatsan.dev.core_domain.usecase.NonSuspendingUseCase

class ValidateWaterQuantityUseCase : NonSuspendingUseCase<String, Pair<Boolean, Int?>>() {
    override fun invoke(
        input: String
    ): Pair<Boolean, Int?> {
        val intWaterAmount = input.toIntOrNull()
        return Pair(input.length <= PlantDataConstraints.WATER_AMOUNT_MAX_LENGTH, intWaterAmount)
    }
}