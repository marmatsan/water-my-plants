package com.marmatsan.dev.catalog_domain.usecase.plant_screen

import com.marmatsan.dev.catalog_domain.model.PlantDataConstraints
import com.marmatsan.dev.core_domain.result.Error
import com.marmatsan.dev.core_domain.result.Result
import com.marmatsan.dev.core_domain.usecase.NonSuspendingUseCase

class ValidateWaterQuantityUseCase : NonSuspendingUseCase<String, Int?, Error>() {
    override fun invoke(
        input: String
    ): Result<Int?, Error> {
        val intWaterAmount = input.toIntOrNull()
        return if (input.length <= PlantDataConstraints.WATER_AMOUNT_MAX_LENGTH) {
            Result.Success(intWaterAmount)
        } else {
            Result.Error()
        }
    }
}