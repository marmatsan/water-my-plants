package com.marmatsan.catalog_data.repository

import com.marmatsan.catalog_data.mapper.toRealmPlant
import com.marmatsan.core_domain.PreferencesData
import com.marmatsan.dev.catalog_domain.model.Plant
import com.marmatsan.dev.catalog_domain.repository.CatalogRepository
import com.marmatsan.dev.core_domain.preferences.Preferences
import com.marmatsan.dev.core_domain.result.Error
import com.marmatsan.dev.core_domain.result.Result
import io.realm.kotlin.Realm
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
class CatalogRepositoryImpl(
    private val realm: Realm,
    private val preferences: Preferences
) : CatalogRepository {
    override suspend fun insertPlant(plant: Plant): Result<Unit, Error> {
        realm.write {
            copyToRealm(plant.toRealmPlant())
        }
        return Result.Success()
    }

    override suspend fun saveIsPlantNameValid(isPlantNameValid : Boolean) {
        preferences.saveIsPlantNameValid(isPlantNameValid)
    }

    override suspend fun saveIsWateringDaysValid(isWateringDaysValid : Boolean) {
        preferences.saveIsWateringDaysValid(isWateringDaysValid)
    }

    override suspend fun saveIsWateringTimeValid(isWateringTimeValid : Boolean) {
        preferences.saveIsWateringTimeValid(isWateringTimeValid)
    }

    override fun getPreferencesFlow(): Flow<PreferencesData> {
        return preferences.preferencesDataFlow()
    }
}