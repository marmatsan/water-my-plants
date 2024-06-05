package com.marmatsan.catalog_data.local

import com.marmatsan.catalog_data.mapper.toPlant
import com.marmatsan.catalog_data.mapper.toRealmPlant
import com.marmatsan.catalog_data.model.RealmPlant
import com.marmatsan.dev.catalog_domain.model.Plant
import com.marmatsan.dev.core_domain.checkIfNull
import com.marmatsan.dev.core_domain.result.Error
import com.marmatsan.dev.core_domain.result.Result
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.BsonObjectId
import org.mongodb.kbson.ObjectId

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

    override suspend fun readPlantById(plantId: String): Flow<Plant> =
        realm.write {
            realm.query<RealmPlant>(
                query = "id == $0",
                args = arrayOf(BsonObjectId(plantId))
            )
        }.asFlow().map { results ->
            results.list.toList().first().toPlant()
        }

    override suspend fun deletePlantById(plantId: String): Flow<Result<Unit, Error>> = flow {
        try {
            val result = realm.write {
                val realmPlantToDelete =
                    query<RealmPlant>(
                        query = "id == $0",
                        args = arrayOf(ObjectId(plantId))
                    ).find().first()
                val foundRealmPlantToDelete = findLatest(realmPlantToDelete)
                foundRealmPlantToDelete.checkIfNull(
                    ifNull = {
                        throw IllegalArgumentException()
                    },
                    ifNotNull = { realmPlant ->
                        delete(realmPlant)
                        Result.Success<Unit, Error>(Unit)
                    }
                )
            }
            emit(result)
        } catch (e: Exception) {
            throw e
        }
    }

    override fun readAllPlantsFlow(): Flow<List<Plant>> =
        realm.query<RealmPlant>().asFlow().map { results ->
            results.list.toList().map { realmPlant ->
                realmPlant.toPlant()
            }
        }

}