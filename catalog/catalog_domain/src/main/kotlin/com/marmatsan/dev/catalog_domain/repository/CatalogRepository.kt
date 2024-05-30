package com.marmatsan.dev.catalog_domain.repository

import com.marmatsan.core_domain.PreferencesData
import com.marmatsan.dev.catalog_domain.model.Plant
import com.marmatsan.dev.core_domain.result.Error
import com.marmatsan.dev.core_domain.result.Result
import kotlinx.coroutines.flow.Flow

interface CatalogRepository {
    suspend fun createPlant(plant: Plant): Result<Unit, Error>
    fun readAllPlantsFlow(): Flow<List<Plant>>
    suspend fun readPlantById(plantId: String): Flow<Plant>
    suspend fun deletePlant(plant: Plant): Result<Unit, Error>
    suspend fun saveIsPlantNameValid(isPlantNameValid: Boolean)
    suspend fun saveIsWateringDaysValid(isWateringDaysValid: Boolean)
    suspend fun saveIsWateringTimeValid(isWateringTimeValid: Boolean)
    fun getPreferencesFlow(): Flow<PreferencesData>
}