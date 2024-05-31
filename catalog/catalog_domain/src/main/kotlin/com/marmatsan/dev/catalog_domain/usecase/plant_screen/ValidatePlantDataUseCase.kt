package com.marmatsan.dev.catalog_domain.usecase.plant_screen

import com.marmatsan.dev.core_domain.usecase.NonSuspendingUseCase
import java.time.DayOfWeek
import java.time.LocalTime

class ValidatePlantDataUseCase :
    NonSuspendingUseCase<ValidatePlantDataParameters, Boolean>() {
    override fun invoke(
        input: ValidatePlantDataParameters
    ): Boolean {
        val plantNameIsValid = !input.plantName.isNullOrBlank() && input.plantName.isNotEmpty()
        val wateringDaysListIsValid = !input.wateringDaysList.isNullOrEmpty()
        val wateringTimeIsValid = input.wateringTime != null

        return plantNameIsValid && wateringDaysListIsValid && wateringTimeIsValid
    }
}

data class ValidatePlantDataParameters(
    val plantName: String?,
    val wateringDaysList: List<DayOfWeek>?,
    val wateringTime: LocalTime?
)