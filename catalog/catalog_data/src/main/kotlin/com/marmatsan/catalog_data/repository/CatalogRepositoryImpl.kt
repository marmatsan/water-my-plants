package com.marmatsan.catalog_data.repository

import com.marmatsan.catalog_data.mapper.toPlant
import com.marmatsan.catalog_data.mapper.toRealmPlant
import com.marmatsan.catalog_data.model.RealmPlant
import com.marmatsan.dev.catalog_domain.model.Plant
import com.marmatsan.dev.catalog_domain.repository.CatalogRepository
import com.marmatsan.dev.core_domain.result.Error
import com.marmatsan.dev.core_domain.result.Result
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.tatarka.inject.annotations.Inject
import org.mongodb.kbson.ObjectId

@Inject
class CatalogRepositoryImpl(
    private val realm: Realm
) : CatalogRepository {
    override suspend fun createPlant(plant: Plant): Result<Unit, Error> {
        realm.write {
            copyToRealm(plant.toRealmPlant())
        }
        return Result.Success()
    }

    override suspend fun readPlantById(plantId: String): Flow<Plant> =
        realm.write {
            realm.query<RealmPlant>(
                query = "id == $0",
                args = arrayOf(ObjectId(plantId))
            )
        }.asFlow().map { results ->
            results.list.toList().first().toPlant()
        }

    override suspend fun deletePlant(plant: Plant): Result<Unit, Error> {
        realm.write {
            val latestPlant = findLatest(plant.toRealmPlant()) ?: return@write
            delete(latestPlant)
        }
        return Result.Success()
    }

    override fun readAllPlantsFlow(): Flow<List<Plant>> =
        realm.query<RealmPlant>().asFlow().map { results ->
            results.list.toList().map { realmPlant ->
                realmPlant.toPlant()
            }
        }
}