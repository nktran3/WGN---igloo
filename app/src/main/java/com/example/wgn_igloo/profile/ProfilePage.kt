package com.example.wgn_igloo.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wgn_igloo.auth.LoginActivity
import com.example.wgn_igloo.database.Members
import com.example.wgn_igloo.R
import com.example.wgn_igloo.databinding.FragmentProfileBinding
import com.example.wgn_igloo.databinding.FragmentProfilePageBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.wgn_igloo.profile.ProfileItemAdapter.ProfileItem
import com.google.firebase.firestore.ktx.firestore

private const val TAG = "ProfilePage"

class ProfilePage : Fragment() {

    private lateinit var profileAdapter: ProfileItemAdapter
    private lateinit var binding: FragmentProfilePageBinding
//    private lateinit var binding: FragmentProfileBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val myDataset: List<ProfileItem> = listOf(
        ProfileItem(R.drawable.profile_icon, "Profile"),
        ProfileItem(R.drawable.friends_icon, "Friends"),
        ProfileItem(R.drawable.support_icon, "Support"),
        ProfileItem(R.drawable.faq_icon, "FAQ"),
        ProfileItem(R.drawable.setting_icon, "Settings"),
        ProfileItem(R.drawable.about_icon, "About Us"),
        ProfileItem(R.drawable.logout_icon, "Logout")

    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Firebase Auth
        auth = Firebase.auth
        // Initialize Google Sign-In options
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(com.firebase.ui.auth.R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso.
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfilePageBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = view.findViewById(R.id.profile_recycler)
        recyclerView.layoutManager = LinearLayoutManager(context)
        profileAdapter = ProfileItemAdapter(myDataset) { item ->
            when (item.text) {
                "Profile" -> showProfileDetails()

                "Friends" -> showFriends()

                "Support" -> openGoogleApp()

                "FAQ" -> showFAQpage()

                "Settings" -> showSettings()

                "About Us" -> showAbout()

                "Logout" -> {
                    signOut()
                    goToLoginActivity()
                    Log.d(TAG, "Signed out")
                }


            }
        }
        recyclerView.adapter = profileAdapter
        // PREVIOUS IMPLEMENTATION
        fetchUsername()

    }
    private fun fetchUsername() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.d(TAG, "No user logged in")
            // Possibly update the UI to reflect that no user is logged in
            updateUsernameOnUI(null, null, null)  // Show "Unknown User" or prompt login
            return
        }

        val userDocRef = Firebase.firestore.collection("users").document(userId)
        userDocRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val givenName = documentSnapshot.getString("givenName")
                val familyName = documentSnapshot.getString("familyName")
                val name = "$givenName $familyName"
                val username = documentSnapshot.getString("username")
                val email = documentSnapshot.getString("email")
                updateUsernameOnUI(username, email, name)
            } else {
                Log.d(TAG, "No such document")
                updateUsernameOnUI(null, null, null)  // Handle case where document is missing
            }
        }.addOnFailureListener { exception ->
            Log.d(TAG, "Error getting documents: ", exception)
            updateUsernameOnUI(null, null, null)  // Handle error case
        }
    }

    private fun updateUsernameOnUI(username: String?, email: String?, name: String?) {
        activity?.runOnUiThread {
            if (username == null) {
                binding.accountName.text = "Unknown User"
                // Optionally show a login button or a message prompting to log in
                Toast.makeText(context, "Please log in to view profile.", Toast.LENGTH_LONG).show()
            } else {
//                binding.nameTextView.text = name ?: "Unknown"  // Correctly reference using View Binding
//                binding.usernameTextView.text = username ?: "Unknown"
//                binding.emailTextView.text = email ?: "Unknown"
                binding.accountName.text = name ?: "Unknown User"
            }
        }
    }


    private fun showFriends() {
        val membersFragment = Members()
        requireActivity().supportFragmentManager.beginTransaction().replace(R.id.fragment_container, membersFragment).commit()
    }

    private fun showSettings(){
        val settingsFragment = SettingsFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, settingsFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showAbout(){
        val aboutFragment = AboutFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, aboutFragment)
            .addToBackStack(null)
            .commit()
    }
    private fun showFAQpage(){
        val faqFragment = FaqFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, faqFragment)
            .addToBackStack(null)
            .commit()

    }

    private fun showProfileDetails(){
        val profileFragment = ProfileFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, profileFragment)
            .addToBackStack(null)
            .commit()

    }
    private fun openGoogleApp() {
        try {
            // URL for the Google Form
            val formUrl = "https://docs.google.com/forms/d/e/1FAIpQLSci-UHF6PjLyTdzAEr_5TKxWdTbQQi8jx7Y8HsXbPrypzTmeQ/viewform"
            val gmmIntentUri = Uri.parse("googlechrome://navigate?url=$formUrl")
            val intent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            intent.setPackage("com.google.android.apps.chrome")
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback to opening the form in any available browser if Chrome is not installed
            val gmmIntentUri =
                Uri.parse("https://docs.google.com/forms/d/e/1FAIpQLSci-UHF6PjLyTdzAEr_5TKxWdTbQQi8jx7Y8HsXbPrypzTmeQ/viewform")
            val intent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            startActivity(intent)
        }
    }

    private fun goToLoginActivity() {
        val intent = Intent(activity, LoginActivity::class.java)
        // You can also put extra data to pass to the destination activity
        intent.putExtra("key", "value")
        startActivity(intent)
    }
    private fun signOut() {
        // Firebase sign out
        FirebaseAuth.getInstance().signOut()

        // Google sign out
        googleSignInClient.signOut().addOnCompleteListener {
            // Handle sign out result
        }
    }



}


