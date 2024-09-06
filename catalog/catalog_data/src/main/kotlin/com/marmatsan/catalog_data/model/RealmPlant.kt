package com.marmatsan.catalog_data.model

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.BsonObjectId
import org.mongodb.kbson.ObjectId

class RealmPlant : RealmObject {
    @PrimaryKey
    var id: BsonObjectId? = null
    var name: String? = null
    var wateringDays: String? = null
    var wateringTime: String? = null
    var image: String? = null
    var waterAmount: Int? = null
    var size: String? = null
    var description: String? = null
    var shortDescription: String? = null
    var watered: Boolean? = null
}