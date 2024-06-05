package com.marmatsan.catalog_data.local

import com.marmatsan.dev.catalog_domain.model.Plant
import com.marmatsan.dev.core_domain.result.Error
import com.marmatsan.dev.core_domain.result.Result
import kotlinx.coroutines.flow.Flow

interface DatabaseManagerLocalSource {
    suspend fun createPlant(plant: Plant): Result<Unit, Error>
    fun readAllPlantsFlow(): Flow<List<Plant>>
    suspend fun readPlantById(plantId: String): Flow<Plant>
    suspend fun deletePlantById(plantId: String): Flow<Result<Plant, Error>>
}