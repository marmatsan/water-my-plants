package com.marmatsan.catalog_data.repository

import com.marmatsan.catalog_data.mapper.toPlant
import com.marmatsan.catalog_data.mapper.toRealmPlant
import com.marmatsan.catalog_data.model.RealmPlant
import com.marmatsan.core_domain.PreferencesData
import com.marmatsan.dev.catalog_domain.model.Plant
import com.marmatsan.dev.catalog_domain.repository.CatalogRepository
import com.marmatsan.dev.core_domain.preferences.Preferences
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
    private val realm: Realm,
    private val preferences: Preferences
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


    override suspend fun saveIsPlantNameValid(isPlantNameValid: Boolean) {
        preferences.saveIsPlantNameValid(isPlantNameValid)
    }

    override suspend fun saveIsWateringDaysValid(isWateringDaysValid: Boolean) {
        preferences.saveIsWateringDaysValid(isWateringDaysValid)
    }

    override suspend fun saveIsWateringTimeValid(isWateringTimeValid: Boolean) {
        preferences.saveIsWateringTimeValid(isWateringTimeValid)
    }

    override fun getPreferencesFlow(): Flow<PreferencesData> {
        return preferences.preferencesDataFlow()
    }
}