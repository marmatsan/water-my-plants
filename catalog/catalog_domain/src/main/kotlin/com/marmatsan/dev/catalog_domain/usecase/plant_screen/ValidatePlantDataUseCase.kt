package com.marmatsan.dev.catalog_domain.usecase.plant_screen

import com.marmatsan.dev.core_domain.result.Error
import com.marmatsan.dev.core_domain.result.Result
import com.marmatsan.dev.core_domain.usecase.NonSuspendingUseCase

class ValidatePlantDataUseCase :
    NonSuspendingUseCase<ValidatePlantDataParameters, Unit, ValidatePlantDataUseCaseError>() {
    override fun invoke(
        input: ValidatePlantDataParameters
    ): Result<Unit, ValidatePlantDataUseCaseError> {
        return if (input.isPlantNameValid && input.isWateringDaysValid && input.isWateringTimeValid) {
            Result.Success()
        } else {
            Result.Error()
        }
    }
}

data class ValidatePlantDataParameters(
    val isPlantNameValid: Boolean,
    val isWateringDaysValid: Boolean,
    val isWateringTimeValid: Boolean
)

sealed interface ValidatePlantDataUseCaseError : Error