package com.marmatsan.water_my_plants

import android.app.Application
import com.marmatsan.water_my_plants.di.AppComponent
import com.marmatsan.water_my_plants.di.createAppComponent

class App : Application() {
    val component: AppComponent by lazy { createAppComponent() }
}