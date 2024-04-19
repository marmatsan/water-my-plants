package com.marmatsan.dev.catalog_domain.model

import android.net.Uri
import java.time.DayOfWeek
import java.time.LocalTime

data class Plant(
    val image: Uri? = null,
    val name: String? = null,
    val wateringDays: List<DayOfWeek>? = null,
    val wateringTime: LocalTime? = null,
    val waterAmount: Int? = null,
    val size: PlantSize? = null,
    val description: String? = null,
    val shortDescription: String? = null,
    val watered: Boolean? = null
)

enum class PlantSize {
    SMALL,
    MEDIUM,
    LARGE,
    EXTRA_LARGE
}

object PlantDataConstraints {
    const val PLANT_NAME_MAX_LENGTH = 100
    const val WATER_AMOUNT_MAX_LENGTH = 4
    const val DESCRIPTION_MAX_LENGTH = 5000
}