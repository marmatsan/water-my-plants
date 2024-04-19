package com.marmatsan.dev.watermyplants.di

import android.content.Context
import com.marmatsan.catalog_data.di.CatalogDataComponent
import com.marmatsan.catalog_data.model.RealmPlant
import com.marmatsan.dev.catalog_domain.di.CatalogDomainComponent
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@Component
@Singleton
abstract class ApplicationComponent(
    @get:Provides val context: Context,
) : CatalogDomainComponent, CatalogDataComponent {
    @Provides
    @Singleton
    fun provideRealmDatabase(): Realm {
        return Realm.open(
            configuration = RealmConfiguration.create(
                schema = setOf(
                    RealmPlant::class
                )
            )
        )
    }
}

interface ApplicationComponentProvider {
    val component: ApplicationComponent
}

val Context.applicationComponent get() = (applicationContext as ApplicationComponentProvider).component
