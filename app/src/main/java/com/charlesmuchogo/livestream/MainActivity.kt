package com.charlesmuchogo.livestream

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.charlesmuchogo.livestream.presentation.favourites.FavouritesFragment
import com.charlesmuchogo.livestream.presentation.home.HomeFragment
import com.charlesmuchogo.livestream.presentation.live.LiveFragment
import com.charlesmuchogo.livestream.presentation.player.PlayerFragment
import com.google.android.material.bottomnavigation.BottomNavigationView


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
        navController.navigate(R.id.action_homeFragment_to_playerFragment)
       // loadFragment(HomeFragment())


        bottomNavigation.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.bbn_home -> HomeFragment()
                R.id.bbn_live -> LiveFragment()
                R.id.bbn_favorite -> FavouritesFragment()
               // R.id.bbn_favorite -> PlayerFragment()
                else -> HomeFragment()
            }

            loadFragment(fragment)
            true
        }

    }
    private  fun loadFragment(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container,fragment)
        transaction.commit()
    }
}