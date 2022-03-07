package com.example.modularizationtest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navInflater = navHost.navController.navInflater
        val navGraph = navInflater.inflate(R.navigation.nav_graph)
        navGraph.setStartDestination(R.id.signInFragment)
        navHost.navController.graph = navGraph

    }
}