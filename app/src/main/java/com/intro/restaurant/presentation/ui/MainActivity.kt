package com.intro.restaurant.presentation.ui

import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.libraries.places.api.Places
import com.intro.restaurant.R
import com.intro.restaurant.presentation.ui.LoginActivity.Companion.getUserType
import me.ibrahimsn.lib.SmoothBottomBar

class MainActivity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContentView(R.layout.activity_main)
                Places.initialize(applicationContext, "AIzaSyCT1LawqV43MkqtMqwUuKVSArVpRNdQ5aA")

                val bottomNavigationView: BottomNavigationView
                val menu: Menu

                if (getUserType() == "Regular") {
                        bottomNavigationView = findViewById(R.id.bottom_navigation1)
                        menu = bottomNavigationView.menu
                } else {
                        bottomNavigationView = findViewById(R.id.bottom_navigation_owner1)
                        menu = bottomNavigationView.menu
                }

                if( getUserType().equals("Regular") ) {
                        val bottomNavigationView = findViewById<SmoothBottomBar>(R.id.bottom_navigation)
                        bottomNavigationView.visibility = View.VISIBLE
                        val navController = findNavController(this, R.id.nav_host_fragment)

                        bottomNavigationView.setupWithNavController(menu , navController)
                }else{
                        val bottomNavigationView = findViewById<me.ibrahimsn.lib.SmoothBottomBar>(R.id.bottom_navigation_owner)
                        bottomNavigationView.visibility = View.VISIBLE
                        val navController = findNavController(this, R.id.nav_host_fragment)
                        bottomNavigationView.setupWithNavController( menu,navController)
                }



        }

}
