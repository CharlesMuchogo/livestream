package com.charlesmuchogo.livestream

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {
  private lateinit var bottomNavigation: BottomNavigationView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
         bottomNavigation = findViewById(R.id.bottomNav)

        loadFragment(HomeFragment())

         bottomNavigation.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.bbn_home -> HomeFragment()
                R.id.bbn_live -> LiveFragment()
                R.id.bbn_favorite -> FavouritesFragment()
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