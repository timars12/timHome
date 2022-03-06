package com.example.authdynamic

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.authdynamic.di.DaggerAuthenticationComponent
import com.example.core.coreComponent
import com.example.module1.R
import retrofit2.Retrofit
import javax.inject.Inject

class LoginActivity : AppCompatActivity() {
    @Inject
    lateinit var retrofit: Retrofit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerAuthenticationComponent.factory().create(this.coreComponent()).inject(this)
        setContentView(R.layout.activity_main)
        val aa = retrofit.baseUrl()
        Log.e("0707", "dynamic module")
    }
}