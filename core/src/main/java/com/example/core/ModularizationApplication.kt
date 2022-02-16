package com.example.core

import android.app.Activity
import android.app.Application
import android.content.Context
import com.example.core.di.CoreComponent
import com.example.core.di.DaggerCoreComponent

class ModularizationApplication: Application() {

    private val coreComponent = DaggerCoreComponent.create()

    companion object {
        @JvmStatic fun coreComponent(context: Context): CoreComponent =
            (context.applicationContext as ModularizationApplication).coreComponent
    }
}

fun Activity.coreComponent() = ModularizationApplication.coreComponent(this)