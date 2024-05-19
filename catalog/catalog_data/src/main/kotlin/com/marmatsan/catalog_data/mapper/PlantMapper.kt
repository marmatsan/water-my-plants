package com.marmatsan.catalog_data.mapper

import android.net.Uri
import com.marmatsan.catalog_data.model.RealmPlant
import com.marmatsan.dev.catalog_domain.model.Plant
import com.marmatsan.dev.catalog_domain.model.PlantSize
import com.marmatsan.dev.core_domain.Empty
import java.time.DayOfWeek

fun Plant.toRealmPlant(): RealmPlant {
    return RealmPlant().apply {
        image = this@toRealmPlant.image?.toString()
        name = this@toRealmPlant.name
        wateringDays = this@toRealmPlant.wateringDays?.joinToString(",") { it.name }
        wateringTime = this@toRealmPlant.wateringTime?.toString()
        waterAmount = this@toRealmPlant.waterAmount
        size = this@toRealmPlant.size?.name
        description = this@toRealmPlant.description
        shortDescription = this@toRealmPlant.shortDescription
        watered = this@toRealmPlant.watered
    }
}

fun RealmPlant.toPlant(): Plant {
    return Plant(
        image = this@toPlant.image?.let { Uri.parse(it) },
        name = this@toPlant.name ?: String.Empty,
        wateringDays = this@toPlant.wateringDays?.split(",")
            ?.mapNotNull {
                it.trim().takeIf { it.isNotEmpty() }
            } // Ensure non-empty and non-null values
            ?.map { day ->
                try {
                    DayOfWeek.valueOf(day.uppercase())
                } catch (e: IllegalArgumentException) {
                    null // Skip invalid day names
                }
            }?.filterNotNull(), // Filter out any remaining null values,
        waterAmount = this@toPlant.waterAmount,
        size = this@toPlant.size?.let {
            try {
                PlantSize.valueOf(it.uppercase().replace(" ", "_"))
            } catch (e: IllegalArgumentException) {
                null // Handle invalid plant size values
            }
        },
        description = this@toPlant.description,
        shortDescription = this@toPlant.shortDescription ?: String.Empty,
        watered = this@toPlant.watered
    )
}