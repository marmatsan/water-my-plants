package com.marmatsan.dev.catalog_domain.repository

import com.marmatsan.dev.catalog_domain.model.Plant
import kotlinx.coroutines.flow.Flow

interface CatalogRepository {
    suspend fun createPlant(plant: Plant)
    fun readAllPlantsFlow(): Flow<List<Plant>>
    fun readPlantById(plantId: String): Plant
    suspend fun deletePlantById(plantId: String)
}