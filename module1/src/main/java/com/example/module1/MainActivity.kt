package com.example.module1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.core.coreComponent
import com.example.core.di.CoreComponent
import com.example.module1.di.DaggerModule1Component
import com.example.module1.di.Module1Component
import com.example.module1.di.SomeApi
import retrofit2.Retrofit
import javax.inject.Inject

class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var retrofit: Retrofit

    @Inject
    lateinit var api: SomeApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerModule1Component.factory().create(this.coreComponent()).inject(this)
        setContentView(R.layout.activity_main)
        val aa = retrofit.baseUrl()
        Log.e("0707", aa.toString())
        Log.e("0707 --", api.toString())

    }
}