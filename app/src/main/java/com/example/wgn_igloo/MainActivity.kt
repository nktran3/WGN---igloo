package com.example.wgn_igloo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.example.wgn_igloo.databinding.ActivityMainBinding
import com.firebase.ui.auth.AuthUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var selectedFragment: Fragment
    private lateinit var firestoreHelper: FirestoreHelper

    //Firebase Instance Variables
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bottomNavigation = findViewById(R.id.bottom_navigation)

        bottomNavigation.selectedItemId= R.id.home_nav

        // Initialize FirestoreHelper
        firestoreHelper = FirestoreHelper(this)

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

        // Initialize Firebase Auth and check if the user is signed in
        auth = Firebase.auth
        if (auth.currentUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
            return
        }

    }

    // We want to check to see if user is signed in even onStart()
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in.
        Log.d(TAG, "${auth.currentUser}")
        if (auth.currentUser == null) {
            // Not signed in, launch the Sign In activity
            Log.d(TAG, "User is not signed in")
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
            return
        }
    }


    private fun signOut() {
        AuthUI.getInstance().signOut(this)
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
        Log.d(TAG, "Successfully signed out")
    }
}