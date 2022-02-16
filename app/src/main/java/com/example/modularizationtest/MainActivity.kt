package com.example.modularizationtest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.Directory.PACKAGE_NAME
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent("com.jeroenmols.modularization.dashboard.open")
            .setPackage(this.packageName)

        findViewById<Button>(R.id.goToModuleActivity).apply {
            setOnClickListener {
                startActivity(intent)
            }
        }
    }
}