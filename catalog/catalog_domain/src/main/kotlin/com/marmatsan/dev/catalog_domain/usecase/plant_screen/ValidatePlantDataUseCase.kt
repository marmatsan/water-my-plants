package com.marmatsan.dev.catalog_domain.usecase.plant_screen

import com.marmatsan.dev.core_domain.usecase.NonSuspendingUseCase
import java.time.DayOfWeek
import java.time.LocalTime

class ValidatePlantDataUseCase :
    NonSuspendingUseCase<ValidatePlantDataParameters, Boolean>() {
    override fun invoke(
        input: ValidatePlantDataParameters
    ): Boolean {
        if (input.plantName == null || input.wateringDaysList == null || input.wateringTime == null) {
            return false
        }

        val plantNameIsValid = input.plantName.isNotBlank() && input.plantName.isNotEmpty()
        val wateringDaysListIsValid = input.wateringDaysList.isNotEmpty()

        return plantNameIsValid && wateringDaysListIsValid
    }
}

data class ValidatePlantDataParameters(
    val plantName: String?,
    val wateringDaysList: List<DayOfWeek>?,
    val wateringTime: LocalTime?
)