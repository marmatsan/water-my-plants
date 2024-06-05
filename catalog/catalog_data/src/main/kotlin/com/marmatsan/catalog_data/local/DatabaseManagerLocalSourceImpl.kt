package com.marmatsan.catalog_data.local

import com.marmatsan.catalog_data.mapper.toPlant
import com.marmatsan.catalog_data.mapper.toRealmPlant
import com.marmatsan.catalog_data.model.RealmPlant
import com.marmatsan.dev.catalog_domain.model.Plant
import com.marmatsan.dev.core_domain.result.Error
import com.marmatsan.dev.core_domain.result.Result
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.BsonObjectId

class DatabaseManagerLocalSourceImpl(
    private val realm: Realm
) : DatabaseManagerLocalSource {
    override suspend fun createPlant(plant: Plant): Result<Unit, Error> {
        realm.write {
            val realmPlantToCreate = plant.toRealmPlant()
            copyToRealm(realmPlantToCreate)
        }
        return Result.Success()
    }

    override fun readPlantById(plantId: String): Plant {
        val realmPlant = realm.query<RealmPlant>(
            query = "id == $0",
            args = arrayOf(BsonObjectId(plantId))
        ).find().firstOrNull()

        return realmPlant?.toPlant()
            ?: throw NoSuchElementException("Plant with id $plantId not found")
    }

    override suspend fun deletePlantById(plantId: String) {
        val realmPlantToDelete = realm.query<RealmPlant>(
            query = "id == $0",
            args = arrayOf(BsonObjectId(plantId))
        ).find().firstOrNull() ?: return

        realm.write {
            val latestRealmPlantToDelete = findLatest(realmPlantToDelete) ?: return@write
            delete(latestRealmPlantToDelete)
        }
    }

    override fun readAllPlantsFlow(): Flow<List<Plant>> =
        realm.query<RealmPlant>().asFlow().map { results ->
            results.list.toList().map { realmPlant ->
                realmPlant.toPlant()
            }
        }

}