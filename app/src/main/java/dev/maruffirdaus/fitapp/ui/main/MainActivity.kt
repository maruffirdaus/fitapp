package dev.maruffirdaus.fitapp.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import dev.maruffirdaus.fitapp.R
import dev.maruffirdaus.fitapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setNavigation()
    }

    private fun setNavigation() {
        val navHost = supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment
        val navController = navHost.navController
        binding.bottomNavigation.setupWithNavController(navController)
    }
}