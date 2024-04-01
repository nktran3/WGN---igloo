package com.example.wgn_igloo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var selectedFragment: Fragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigation = findViewById(R.id.bottom_navigation)

        bottomNavigation.selectedItemId= R.id.home_nav

        bottomNavigation.setOnItemSelectedListener{
            if (it.itemId == R.id.recipe_nav) {
                selectedFragment = RecipesPage()
            } else if (it.itemId == R.id.shopping_list_nav) {
                selectedFragment = ShoppingListPage()
            } else if (it.itemId == R.id.home_nav) {
                selectedFragment = HomePage()
            } else if (it.itemId == R.id.inbox_nav) {
                selectedFragment = InboxPage()
            } else if (it.itemId == R.id.profile_nav) {
                selectedFragment = ProfilePage()
            }
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, selectedFragment).commit()

            true
        }




    }
}