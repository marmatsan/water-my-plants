package com.marmatsan.catalog_data.local

import com.marmatsan.dev.catalog_domain.model.Plant
import kotlinx.coroutines.flow.Flow

interface DatabaseManagerLocalSource {
    suspend fun createPlant(plant: Plant)
    fun readAllPlantsFlow(): Flow<List<Plant>>
    fun readPlantById(plantId: String): Plant
    suspend fun deletePlantById(plantId: String)
}