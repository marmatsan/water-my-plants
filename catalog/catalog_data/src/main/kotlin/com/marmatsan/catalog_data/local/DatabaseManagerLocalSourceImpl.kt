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
import kotlinx.coroutines.flow.catch
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
        realm.query<RealmPlant>(
            query = "id == $0",
            args = arrayOf(BsonObjectId(plantId))
        ).asFlow().map { results ->
            results.list.toList().first().toPlant()
        }

    override suspend fun deletePlantById(plantId: String): Flow<Result<Plant, Error>> =
        realm.query<RealmPlant>(
            query = "id == $0",
            args = arrayOf(ObjectId(plantId))
        ).asFlow()
            .catch { error ->

            }
            .map { results ->
                val deletedPlant = results.list.toList().first().toPlant()
                Result.Success(deletedPlant)
            }

    override fun readAllPlantsFlow(): Flow<List<Plant>> =
        realm.query<RealmPlant>().asFlow().map { results ->
            results.list.toList().map { realmPlant ->
                realmPlant.toPlant()
            }
        }

}