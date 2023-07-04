package com.charlesmuchogo.livestream

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.charlesmuchogo.livestream.presentation.favourites.FavouritesFragment
import com.charlesmuchogo.livestream.presentation.home.HomeFragment
import com.charlesmuchogo.livestream.presentation.live.LiveFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigation: BottomNavigationView
    lateinit var navController: NavController


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottomNavigation = findViewById(R.id.bottomNav)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController


        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.playerFragment) {
                bottomNavigation.visibility = View.GONE
            } else {
                bottomNavigation.visibility = View.VISIBLE
            }
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            val destination = when (item.itemId) {
                R.id.bbn_home -> R.id.homeFragment
                R.id.bbn_live -> R.id.liveFragment
                R.id.bbn_favorite -> R.id.favouritesFragment
                else -> R.id.homeFragment
            }

            navController.navigate(destination)
            true
        }
    }
}
