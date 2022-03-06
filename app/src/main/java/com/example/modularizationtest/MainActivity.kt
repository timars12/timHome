package com.example.modularizationtest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent("com.jeroenmols.modularization.login.open")
            .setPackage(this.packageName)

        findViewById<Button>(R.id.goToModuleActivity).apply {
            setOnClickListener {
                startActivity(intent)
            }
        }
    }
}