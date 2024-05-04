package com.example.wgn_igloo.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.example.wgn_igloo.R
import com.example.wgn_igloo.database.FirestoreHelper
import com.example.wgn_igloo.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private lateinit var firestoreHelper: FirestoreHelper
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var toolbarProfile: Toolbar

    companion object {
        private const val TAG = "ProfileFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firestoreHelper = FirestoreHelper(requireContext())


        binding.saveButton.setOnClickListener {
            val newUsername = binding.usernameEditText.text.toString()
            if (newUsername.isNotBlank()) {
                updateUsername(newUsername)
                binding.usernameTextView.text = newUsername // Update the displayed username immediately
            } else {
                Toast.makeText(context, "Username cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        currentUserId?.let {
            fetchUserData(it)
        }

        toolbarProfile = binding.toolbarProfile
        updateToolbar()
    }

    private fun updateToolbar() {
        toolbarProfile.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.back_icon)
        toolbarProfile.setNavigationOnClickListener { activity?.onBackPressed() }

    }
    private fun updateUsername(newUsername: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let {
            firestoreHelper.updateUsername(it, newUsername, onSuccess = {
                Toast.makeText(context, "Username updated successfully", Toast.LENGTH_SHORT).show()
            }, onFailure = { exception ->
                Toast.makeText(context, "Failed to update username", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Failed to update username", exception)
            })
        }
    }

    private fun fetchUserData(userId: String) {
        firestoreHelper.getUser(userId, onSuccess = { user ->

            binding.emailTextView.text = user.email
            binding.uidTextView.text = user.uid
            binding.usernameTextView.text = user.username // Display username in the TextView
            binding.usernameEditText.setText(user.username)

        }, onFailure = { exception ->
            Log.e(TAG, "Failed to fetch user data", exception)
            Toast.makeText(context, "Failed to load user data", Toast.LENGTH_SHORT).show()
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Prevent memory leaks
    }
}
