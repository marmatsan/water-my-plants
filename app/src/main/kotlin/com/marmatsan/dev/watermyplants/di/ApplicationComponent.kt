package com.marmatsan.dev.watermyplants.di

import android.content.Context
import com.marmatsan.catalog_data.di.CatalogDataComponent
import com.marmatsan.catalog_data.model.RealmPlant
import com.marmatsan.dev.catalog_domain.di.CatalogDomainComponent
import com.marmatsan.dev.core_domain.di.Singleton
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.dynamic.DynamicMutableRealmObject
import io.realm.kotlin.dynamic.DynamicRealmObject
import io.realm.kotlin.dynamic.getValue
import io.realm.kotlin.migration.AutomaticSchemaMigration
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import org.mongodb.kbson.ObjectId

@Component
@Singleton
abstract class ApplicationComponent(
    @get:Provides val context: Context,
) : CatalogDomainComponent, CatalogDataComponent {
    @Provides
    @Singleton
    fun provideRealmDatabase(): Realm {

        val migration = AutomaticSchemaMigration { migrationContext ->
            migrationContext.enumerate("RealmPlant") { oldObject: DynamicRealmObject, newObject: DynamicMutableRealmObject? ->
                newObject?.run {
                    // Rename property
                    set("id", oldObject.getValue<ObjectId>("_id"))
                }
            }
        }

        return Realm.open(
            configuration = RealmConfiguration
                .Builder(
                    schema = setOf(
                        RealmPlant::class
                    )
                )
                .schemaVersion(2)
                .migration(migration)
                .build()
        )
    }
}

interface ApplicationComponentProvider {
    val component: ApplicationComponent
}

val Context.applicationComponent get() = (applicationContext as ApplicationComponentProvider).component