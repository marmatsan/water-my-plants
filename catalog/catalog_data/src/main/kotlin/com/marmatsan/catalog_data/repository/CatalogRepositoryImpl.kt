package com.marmatsan.catalog_data.repository

import com.marmatsan.catalog_data.mapper.toRealmPlant
import com.marmatsan.dev.catalog_domain.model.Plant
import com.marmatsan.dev.catalog_domain.repository.CatalogRepository
import io.realm.kotlin.Realm

class CatalogRepositoryImpl(
    private val realm: Realm
) : CatalogRepository {
    override suspend fun insertPlant(plant: Plant) {
        realm.write {
            copyToRealm(plant.toRealmPlant())
        }
    }
}