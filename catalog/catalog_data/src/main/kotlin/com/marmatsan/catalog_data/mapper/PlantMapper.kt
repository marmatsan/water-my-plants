package com.marmatsan.catalog_data.mapper

import android.net.Uri
import com.marmatsan.catalog_data.model.RealmPlant
import com.marmatsan.dev.catalog_domain.model.Plant
import com.marmatsan.dev.catalog_domain.model.PlantSize
import com.marmatsan.dev.core_domain.Empty
import com.marmatsan.dev.core_domain.isNull
import org.mongodb.kbson.BsonObjectId
import org.mongodb.kbson.ObjectId
import java.time.DayOfWeek
import java.time.LocalTime

fun Plant.toRealmPlant(): RealmPlant {
    return RealmPlant().apply {
        id = BsonObjectId(
            this@toRealmPlant.id.ifEmpty { BsonObjectId().toHexString() }
        )
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
        id = this@toPlant.id?.toHexString() ?: String.Empty,
        image = this@toPlant.image?.let {
            Uri.parse(it)
        },
        name = this@toPlant.name,
        wateringDays = this@toPlant.wateringDays
            ?.split(",")
            ?.map {
                DayOfWeek.valueOf(it)
            },
        wateringTime = this@toPlant.wateringTime?.let {
            LocalTime.parse(it)
        },
        waterAmount = this@toPlant.waterAmount,
        size = this@toPlant.size?.let {
            PlantSize.valueOf(it)
        },
        description = this@toPlant.description,
        shortDescription = this@toPlant.shortDescription,
        watered = this@toPlant.watered
    )
}