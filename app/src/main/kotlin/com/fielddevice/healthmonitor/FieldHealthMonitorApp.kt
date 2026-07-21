package com.fielddevice.healthmonitor

import android.app.Application
import com.fielddevice.healthmonitor.di.AppContainer

class FieldHealthMonitorApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
