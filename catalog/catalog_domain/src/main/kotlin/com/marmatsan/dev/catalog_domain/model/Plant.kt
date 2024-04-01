package com.marmatsan.dev.catalog_domain.model

import android.graphics.Bitmap
import java.time.DayOfWeek
import java.time.LocalTime

data class Plant(
    val image: Bitmap? = null,
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