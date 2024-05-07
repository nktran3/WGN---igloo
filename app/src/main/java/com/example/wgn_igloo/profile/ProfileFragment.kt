package com.example.wgn_igloo.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import android.widget.TextView
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
    private lateinit var toolbarProfileTitle: TextView

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

        binding.editUsername.setOnClickListener {
            if (binding.newUsernameLabel.visibility == View.GONE) {
                binding.newUsernameLabel.visibility = View.VISIBLE
                binding.usernameEditText.visibility = View.VISIBLE
                binding.saveButton.visibility = View.VISIBLE
            } else {
                binding.newUsernameLabel.visibility = View.GONE
                binding.usernameEditText.visibility = View.GONE
                binding.saveButton.visibility = View.GONE
            }
        }

        binding.saveButton.setOnClickListener {
            val newUsername = binding.usernameEditText.text.toString()
            if (newUsername.isNotBlank()) {
                updateUsername(newUsername)
                binding.usernameTextView.text = newUsername // Update the displayed username immediately
                binding.newUsernameLabel.visibility = View.GONE
                binding.usernameEditText.visibility = View.GONE
                binding.saveButton.visibility = View.GONE
            } else {
                Toast.makeText(context, "Username cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        currentUserId?.let {
            fetchUserData(it)
        }

        toolbarProfile = binding.toolbarProfile
        toolbarProfileTitle = binding.toolbarProfileTitle
        updateToolbar()
    }

    private fun updateToolbar() {
        toolbarProfile.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.back_icon)
        toolbarProfile.setNavigationOnClickListener { activity?.onBackPressed() }
        toolbarProfileTitle.text = "Profile"

    }
    private fun updateUsername(newUsername: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let {
            firestoreHelper.updateUsername(it, newUsername, onSuccess = {
                Toast.makeText(context, "Username updated successfully", Toast.LENGTH_SHORT).show()
            }, onFailure = { exception ->
//                Toast.makeText(context, "Failed to update username", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Failed to update username", exception)
            })
        }
    }

    private fun fetchUserData(userId: String) {
        firestoreHelper.getUser(userId, onSuccess = { user ->
            binding.nameTextView.text = user.givenName + " " + user.familyName
            binding.emailTextView.text = user.email
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
