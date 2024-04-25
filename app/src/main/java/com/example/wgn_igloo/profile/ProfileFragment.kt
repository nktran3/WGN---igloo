package com.example.wgn_igloo.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import com.example.wgn_igloo.database.FirestoreHelper
import com.example.wgn_igloo.R

class ProfileFragment : Fragment() {

    private lateinit var firestoreHelper: FirestoreHelper

    companion object {
        private const val TAG = "ProfileFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firestoreHelper = FirestoreHelper(requireContext())

        val editTextUsername = view.findViewById<EditText>(R.id.usernameEditText)
        val textViewUid = view.findViewById<TextView>(R.id.uidTextView)
        val usernameTextView = view.findViewById<TextView>(R.id.usernameTextView)
        val saveButton = view.findViewById<Button>(R.id.saveButton)

        saveButton.setOnClickListener {
            val newUsername = editTextUsername.text.toString()
            if (newUsername.isNotBlank()) {
                updateUsername(newUsername)
//                textViewUid.text = newUsername // Update TextView immediately
                usernameTextView.text = newUsername // Update the displayed username immediately

            } else {
                Toast.makeText(context, "Username cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        currentUserId?.let {
            fetchUserData(it)
        }
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
            view?.findViewById<TextView>(R.id.emailTextView)?.text = user.email
            view?.findViewById<TextView>(R.id.uidTextView)?.text = user.uid
            view?.findViewById<TextView>(R.id.usernameTextView)?.text = user.username // Display username in the TextView
            view?.findViewById<EditText>(R.id.usernameEditText)?.setText(user.username)
        }, onFailure = { exception ->
            Log.e(TAG, "Failed to fetch user data", exception)
            Toast.makeText(context, "Failed to load user data", Toast.LENGTH_SHORT).show()
        })
    }

}
