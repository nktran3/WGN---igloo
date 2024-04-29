package com.example.wgn_igloo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import com.example.wgn_igloo.inbox.InboxPage
import com.example.wgn_igloo.profile.ProfilePage
import com.example.wgn_igloo.recipe.RecipeSearchFragment
import com.example.wgn_igloo.recipe.RecipeViewModel
import com.example.wgn_igloo.recipe.RecipesPage
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import android.Manifest

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

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        askNotificationPermission()


        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            val msg = token.toString()
            Log.d(TAG, msg)
            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        })

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

    // Function used to set up all fragments with tags and hide all except for the home page fragment
    private fun setupFragments() {
        supportFragmentManager.beginTransaction().apply {
            add(R.id.fragment_container, recipesPage, "recipes").hide(recipesPage)
            add(R.id.fragment_container, shoppingListPage, "shoppingList").hide(shoppingListPage)
            add(R.id.fragment_container, inboxPage, "inbox").hide(inboxPage)
            add(R.id.fragment_container, profilePage, "profile").hide(profilePage)
            add(R.id.fragment_container, homePage, "home").hide(homePage)
        }.commit()
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
    private fun switchFragments(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            // Hide all fragments
            supportFragmentManager.fragments.forEach {
                if (it is RecipeSearchFragment) {
                    remove(it)
                } else {
                    hide(it)
                }
            }

            if (!fragment.isAdded) {
                // If the fragment is not added, add it now
                add(R.id.fragment_container, fragment)
            }
            // Show the desired fragment
            show(fragment)
            commit()
        }
        Log.d(TAG, "Fragment switched")
        activeFragment = fragment  // Update the active fragment reference
        activeFragmentTag = fragment.tag ?: "" // Update the active fragment tag

    }
    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }



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