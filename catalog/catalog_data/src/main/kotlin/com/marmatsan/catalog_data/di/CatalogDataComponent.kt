package com.marmatsan.catalog_data.di

import com.marmatsan.catalog_data.repository.CatalogRepositoryImpl
import com.marmatsan.dev.catalog_domain.repository.CatalogRepository
import io.realm.kotlin.Realm
import me.tatarka.inject.annotations.Inject
import me.tatarka.inject.annotations.Provides

interface CatalogDataComponent {
    @Provides
    @Inject
    fun provideCatalogRepository(realm: Realm): CatalogRepository {
        return CatalogRepositoryImpl(
            realm = realm
        )
    }
}