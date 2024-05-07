package com.example.wgn_igloo.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wgn_igloo.database.FirestoreHelper
import com.example.wgn_igloo.MainActivity
import com.example.wgn_igloo.database.User
import com.example.wgn_igloo.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


private const val TAG = "SignInActivity"
class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    // Firebase instance variables
    private lateinit var auth: FirebaseAuth
    //Firestore
    private lateinit var firestoreHelper: FirestoreHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This codelab uses View Binding
        // See: https://developer.android.com/topic/libraries/view-binding
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize FirebaseAuth
        auth = Firebase.auth
        // Initialize FirestoreHelper
        firestoreHelper = FirestoreHelper(this)

        binding.buttonSignUp.setOnClickListener(){
            registerUser()
        }
        binding.textViewLogin.setOnClickListener(){
            goToLogin()
        }
    }

    private fun registerUser() {
        val firstName = binding.firstNameSignup.text.toString().trim()
        val lastName = binding.lastNameSignup.text.toString().trim()
        val usernameInput = binding.usernameSignup.text.toString().trim()
        val email = binding.emailSignup.text.toString().trim()
        val password = binding.passwordSignup.text.toString().trim()
        val confirmPassword = binding.confirmPasswordSignup.text.toString().trim()

        if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword == password) {
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        // Check if username is empty and default to uid if it is
                        val username = if (usernameInput.isEmpty()) it.uid else usernameInput
                        // Using the new User data class structure with givenName, familyName, and username
                        val newUser = User(givenName = firstName, familyName = lastName, email = email, uid = it.uid, username = username)
                        Firebase.firestore.collection("users").document(it.uid)
                            .set(newUser)
                            .addOnSuccessListener {
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to create user: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and Password cannot be empty.", Toast.LENGTH_SHORT).show()
            } else if (confirmPassword != password) {
                Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun goToLogin(){
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

}