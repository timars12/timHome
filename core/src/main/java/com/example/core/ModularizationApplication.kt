package com.example.core

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import com.example.core.di.CoreComponent
import com.example.core.di.DaggerCoreComponent
import com.github.venom.Venom
import com.google.android.play.core.splitcompat.SplitCompat
import com.google.android.play.core.splitcompat.SplitCompatApplication

class ModularizationApplication : SplitCompatApplication() {

    private val coreComponent = DaggerCoreComponent.factory().create(this)

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        SplitCompat.install(this)
    }

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

fun Activity.coreComponent() = ModularizationApplication.coreComponent(this)
fun Fragment.coreComponent() = ModularizationApplication.coreComponent(requireContext())
