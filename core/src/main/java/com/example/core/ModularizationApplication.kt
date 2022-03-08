package com.example.core

import android.app.Activity
import android.app.Application
import android.content.Context
import com.example.core.di.CoreComponent
import com.example.core.di.DaggerCoreComponent
import com.google.android.play.core.splitcompat.SplitCompat

class ModularizationApplication: Application() {

    private val coreComponent = DaggerCoreComponent.create()

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }

    companion object {
        @JvmStatic fun coreComponent(context: Context): CoreComponent =
            (context.applicationContext as ModularizationApplication).coreComponent
    }
}

fun Activity.coreComponent() = ModularizationApplication.coreComponent(this)