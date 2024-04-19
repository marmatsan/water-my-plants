package com.marmatsan.catalog_data.mapper

import com.marmatsan.catalog_data.model.RealmPlant
import com.marmatsan.dev.catalog_domain.model.Plant

fun Plant.toRealmPlant(): RealmPlant {
    return RealmPlant().apply {
        image = image.toString()
        name = name
        wateringDays = wateringDays.toString()
        wateringTime = wateringTime.toString()
        waterAmount = waterAmount
        size = size.toString()
        description = description
        shortDescription = shortDescription
        watered = watered
    }
}

fun RealmPlant.toPlant(): Plant { // TODO
    return Plant()
}