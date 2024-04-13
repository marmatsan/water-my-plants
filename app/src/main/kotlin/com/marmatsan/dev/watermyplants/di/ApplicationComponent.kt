package com.marmatsan.dev.watermyplants.di

import android.content.Context
import com.marmatsan.dev.catalog_domain.di.CatalogComponent
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@Component
@Singleton
abstract class ApplicationComponent(
    @get:Provides val context: Context,
) : CatalogComponent()

interface ApplicationComponentProvider {
    val component: ApplicationComponent
}

val Context.applicationComponent get() = (applicationContext as ApplicationComponentProvider).component
