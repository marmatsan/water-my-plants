package com.marmatsan.android_template

import android.app.Application
import com.marmatsan.android_template.di.AppComponent
import com.marmatsan.android_template.di.createAppComponent

class App : Application() {
    val component: AppComponent by lazy { createAppComponent() }
}