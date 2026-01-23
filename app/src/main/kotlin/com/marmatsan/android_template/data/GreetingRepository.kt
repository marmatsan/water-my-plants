package com.marmatsan.android_template.data

import me.tatarka.inject.annotations.Inject

class GreetingRepository @Inject constructor() {
    fun greeting(name: String) = "Hello $name!"
}