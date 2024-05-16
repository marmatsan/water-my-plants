package com.marmatsan.dev.catalog_domain.usecase.plant_screen

import com.marmatsan.dev.catalog_domain.model.PlantDataConstraints
import com.marmatsan.dev.core_domain.result.Error
import com.marmatsan.dev.core_domain.result.Result
import com.marmatsan.dev.core_domain.usecase.NonSuspendingUseCase

class ValidatePlantNameUseCase : NonSuspendingUseCase<String, Unit, Error>() {
    override operator fun invoke(
        input: String
    ): Result<Unit, Error> {
        return if (input.length <= PlantDataConstraints.PLANT_NAME_MAX_LENGTH) {
            Result.Success()
        } else {
            Result.Error()
        }
    }
}