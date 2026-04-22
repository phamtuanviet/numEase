package com.example.numease

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NumEaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        }
}