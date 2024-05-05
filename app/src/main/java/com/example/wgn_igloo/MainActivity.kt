package com.example.wgn_igloo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.wgn_igloo.auth.LoginActivity
import com.example.wgn_igloo.auth.SignUpActivity
import com.example.wgn_igloo.database.FirestoreHelper
import com.example.wgn_igloo.databinding.ActivityMainBinding
import com.example.wgn_igloo.grocery.ShoppingListPage
import com.example.wgn_igloo.home.HomePage
import com.example.wgn_igloo.home.InventoryDisplayFragment
import com.example.wgn_igloo.inbox.InboxPage
import com.example.wgn_igloo.profile.ProfilePage
import com.example.wgn_igloo.recipe.RecipeSearchFragment
import com.example.wgn_igloo.recipe.RecipeViewModel
import com.example.wgn_igloo.recipe.RecipesPage
import com.firebase.ui.auth.AuthUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var firestoreHelper: FirestoreHelper
    private lateinit var viewModel: RecipeViewModel

    // Fragments
    private val recipesPage by lazy { RecipesPage() }
    private val shoppingListPage by lazy { ShoppingListPage() }
    private val homePage by lazy { HomePage() }
    private val inboxPage by lazy { InboxPage() }
    private val profilePage by lazy { ProfilePage() }


    // Current active fragment
    private var activeFragment: Fragment = homePage

    // Feature fragments
    private var recipeSearchFragment: RecipeSearchFragment? = null

    // Firebase Authentication
    private lateinit var auth: FirebaseAuth

    // Tag of the active fragment
    private var activeFragmentTag: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // newly added
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomePage())
                .commit()
        }

        val callback = object : OnBackPressedCallback(true ) {
            override fun handleOnBackPressed() {
                val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                // If current frag is recipe search set it to null and
                // make sure to set the viewModel data to null to prevent zombie
                if (currentFragment is RecipeSearchFragment) {
                    recipeSearchFragment = null
                    viewModel.currentFragment.value = null
                    switchFragments(recipesPage)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        bottomNavigation = binding.bottomNavigation
        bottomNavigation.selectedItemId = R.id.home_nav
        firestoreHelper = FirestoreHelper(this)

        // Initialize Firebase Auth
        auth = Firebase.auth

        viewModel = ViewModelProvider(this).get(RecipeViewModel::class.java)
        viewModel.currentFragment.observe(this, Observer { fragment ->
            recipeSearchFragment = fragment as RecipeSearchFragment?
        })

        setupFragments()
        setupBottomNavigation()

        // Restore the active fragment in the case of config change
        if (savedInstanceState != null) {
            activeFragmentTag = savedInstanceState.getString("activeFragmentTag", "")
            Log.d(TAG, activeFragmentTag)

            // Restore the active fragment based on the saved tag
            val restoredFragment = supportFragmentManager.findFragmentByTag(activeFragmentTag)
            Log.d(TAG, "$restoredFragment")
            if (restoredFragment != null) {
                switchFragments(restoredFragment)
            }
        }
    }

    fun onCategorySelected(category: String) {
        val inventoryFragment = supportFragmentManager.findFragmentByTag("InventoryDisplay") as? InventoryDisplayFragment
        if (inventoryFragment != null) {
            inventoryFragment.fetchGroceryItemsByCategory(category)
        } else {
            Log.d(TAG, "InventoryDisplayFragment not found or not initialized")
            // You might want to add logic here to add the fragment if it's not found
        }
    }

//    fun onCategorySelected(category: String) {
//        var inventoryFragment = supportFragmentManager.findFragmentByTag("InventoryDisplay") as? InventoryDisplayFragment
//        val fragmentTransaction = supportFragmentManager.beginTransaction()
//
//        if (inventoryFragment == null) {
//            // Fragment not found, adding it to the fragment manager
//            inventoryFragment = InventoryDisplayFragment()
//            fragmentTransaction.add(R.id.fragment_container, inventoryFragment, "InventoryDisplay")
//                .hide(inventoryFragment) // Initially hide the fragment
//        }
//
//        // Condition to show InventoryDisplayFragment
//        // Example: Check if the selected category is not empty
//        val shouldShowInventory = category.isNotEmpty() // Adjust this condition based on your actual requirements
//
//        if (shouldShowInventory) {
//            // Show the InventoryDisplayFragment
//            fragmentTransaction.show(inventoryFragment)
//            activeFragment = inventoryFragment // Set the InventoryDisplayFragment as the current active fragment
//            activeFragmentTag = "InventoryDisplay" // Update the tag of the active fragment
//        } else {
//            // Hide the InventoryDisplayFragment if the condition is not met
//            fragmentTransaction.hide(inventoryFragment)
//        }
//
//        fragmentTransaction.commit()
//    }

    // Function used to set up all fragments with tags and hide all except for the home page fragment
    private fun setupFragments() {
        supportFragmentManager.beginTransaction().apply {
            add(R.id.fragment_container, recipesPage, "recipes").hide(recipesPage)
            add(R.id.fragment_container, shoppingListPage, "shoppingList").hide(shoppingListPage)
            add(R.id.fragment_container, inboxPage, "inbox").hide(inboxPage)
            add(R.id.fragment_container, profilePage, "profile").hide(profilePage)
            add(R.id.fragment_container, InventoryDisplayFragment(), "InventoryDisplay").hide(InventoryDisplayFragment())
            add(R.id.fragment_container, homePage, "home").show(homePage)
        }.commit()
        // Set the initial fragment to homePage
        activeFragment = homePage
        activeFragmentTag = "home"
    }


    // Function used to set up the bottom navigation bar to switch between fragments
    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            val fragmentToShow = when (item.itemId) {
                R.id.recipe_nav -> recipeSearchFragment ?: recipesPage
                R.id.shopping_list_nav -> shoppingListPage
                R.id.home_nav -> homePage
                R.id.inbox_nav -> inboxPage
                R.id.profile_nav -> profilePage
                else -> return@setOnItemSelectedListener false
            }

            switchFragments(fragmentToShow)
            true
        }
    }

    // Function used to peform fragment transactions
//    private fun switchFragments(fragment: Fragment) {
//        supportFragmentManager.beginTransaction().apply {
//            // Hide all fragments
//            supportFragmentManager.fragments.forEach {
//                if (it is RecipeSearchFragment) {
//                    remove(it)
//                } else {
//                    hide(it)
//                }
//            }
//
//            if (!fragment.isAdded) {
//                // If the fragment is not added, add it now
//                add(R.id.fragment_container, fragment)
//            }
//            // Show the desired fragment
//            show(fragment)
//            commit()
//        }
//        Log.d(TAG, "Fragment switched")
//        activeFragment = fragment  // Update the active fragment reference
//        activeFragmentTag = fragment.tag ?: "" // Update the active fragment tag
//    }

    private fun switchFragments(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()

        // Hide all fragments first
        supportFragmentManager.fragments.forEach {
            fragmentTransaction.hide(it)
        }

        // Special handling to show InventoryDisplayFragment only when needed
        if (fragment is HomePage) {
            val inventoryFragment = supportFragmentManager.findFragmentByTag("InventoryDisplay") as? InventoryDisplayFragment
            inventoryFragment?.let { fragmentTransaction.hide(it) } // Ensure InventoryDisplayFragment is hidden when on HomePage
        }

        // Show the selected fragment
        if (!fragment.isAdded) {
            fragmentTransaction.add(R.id.fragment_container, fragment, fragment.tag)
        }
        fragmentTransaction.show(fragment)
        fragmentTransaction.commit()

        activeFragment = fragment
        activeFragmentTag = fragment.tag ?: ""
    }

//    private fun switchFragments(fragment: Fragment) {
//        val fragmentTransaction = supportFragmentManager.beginTransaction()
//
//        // Hide all fragments first
//        supportFragmentManager.fragments.forEach { existingFragment ->
//            fragmentTransaction.hide(existingFragment)
//        }
//
//        if (!fragment.isAdded) {
//            fragmentTransaction.add(R.id.fragment_container, fragment, fragment.tag)
//        }
//        fragmentTransaction.show(fragment)
//
//        // Ensure InventoryDisplayFragment is handled correctly
//        if (fragment is HomePage) {
//            // Check if InventoryDisplayFragment is already added, if not, add it
//            var inventoryFragment = supportFragmentManager.findFragmentByTag("InventoryDisplay") as? InventoryDisplayFragment
//            if (inventoryFragment == null) {
//                inventoryFragment = InventoryDisplayFragment()
//                fragmentTransaction.add(R.id.fragment_container, inventoryFragment, "InventoryDisplay").hide(inventoryFragment)
//            }
//            // Only show InventoryDisplayFragment if needed based on some condition
//            if (someConditionToShowInventory) {
//                fragmentTransaction.show(inventoryFragment)
//            } else {
//                fragmentTransaction.hide(inventoryFragment)
//            }
//        }
//
//        fragmentTransaction.commit()
//        activeFragment = fragment
//        activeFragmentTag = fragment.tag ?: ""
//    }


    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("activeFragmentTag", activeFragmentTag)
    }
}