package com.example.core

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.Context
import com.example.core.di.CoreComponent
import com.example.core.di.DaggerCoreComponent
import com.github.venom.BuildConfig
import com.github.venom.Venom

class ModularizationApplication : Application() {
    private val coreComponent = DaggerCoreComponent.factory().create(this)

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Venom.createInstance(this).apply {
                initialize()
                start()
            }
        }
    }

    companion object {
        @JvmStatic fun coreComponent(context: Context): CoreComponent =
            (context.applicationContext as ModularizationApplication).coreComponent
    }
}

fun Service.coreComponent() = ModularizationApplication.coreComponent(this)

fun Activity.coreComponent() = ModularizationApplication.coreComponent(this)
