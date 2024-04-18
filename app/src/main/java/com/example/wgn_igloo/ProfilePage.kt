package com.example.wgn_igloo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.helper.widget.Carousel
import androidx.fragment.app.Fragment
import com.example.wgn_igloo.databinding.FragmentProfilePageBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlin.math.sign


private const val TAG = "ProfilePage"

class ProfilePage : Fragment() {


    private lateinit var binding: FragmentProfilePageBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

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

        binding.profileButton.setOnClickListener {
            Toast.makeText(context, "Profile clicked!", Toast.LENGTH_SHORT).show()
        }

        binding.settingsButton.setOnClickListener {
            Toast.makeText(context, "Settings clicked!", Toast.LENGTH_SHORT).show()
        }

        binding.membersButton.setOnClickListener {
            val membersFragment = Members()
            requireActivity().supportFragmentManager.beginTransaction().replace(R.id.fragment_container, membersFragment).commit()
        }

        binding.faqButton.setOnClickListener {
            val faqFragment = FaqFragment()
            requireActivity().supportFragmentManager.beginTransaction().replace(R.id.fragment_container, faqFragment).commit()
        }

//        binding.supportButton.setOnClickListener {
//            Toast.makeText(context, "Support clicked!", Toast.LENGTH_SHORT).show()
//        }

        binding.logoutButton.setOnClickListener {
            signOut()
            goToLoginActivity()
            Log.d(TAG, "Signed out")
        }
        
        binding.supportButton.setOnClickListener {
            openGoogleApp()
        }

        return binding.root
    }

    fun goToLoginActivity() {
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

//    private fun openGoogleApp() {
//        try {
//            val gmmIntentUri = Uri.parse("googlechrome://navigate?url=https://www.google.com/")
//            val intent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
//            intent.setPackage("com.google.android.apps.chrome")
//            startActivity(intent)
//        } catch (e: Exception) {
//            val gmmIntentUri = Uri.parse("https://www.google.com/")
//            val intent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
//            startActivity(intent)
//        }
//    }

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
            val gmmIntentUri = Uri.parse("https://docs.google.com/forms/d/e/1FAIpQLSci-UHF6PjLyTdzAEr_5TKxWdTbQQi8jx7Y8HsXbPrypzTmeQ/viewform")
            val intent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            startActivity(intent)
        }
    }


}