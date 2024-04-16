package com.example.wgn_igloo

import android.content.Intent
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


private const val TAG = "ProfilePage"

class ProfilePage : Fragment() {


    private lateinit var binding: FragmentProfilePageBinding

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Firebase Auth
        auth = Firebase.auth
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
            //signOut()
            goToLoginActivity()
            Log.d(TAG, "Signed out")
        }
    }

    fun goToLoginActivity() {
        val intent = Intent(activity, LoginActivity::class.java)
        // You can also put extra data to pass to the destination activity
        intent.putExtra("key", "value")
        startActivity(intent)
    }

}